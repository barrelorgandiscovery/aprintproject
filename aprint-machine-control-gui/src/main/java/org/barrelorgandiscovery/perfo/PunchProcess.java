package org.barrelorgandiscovery.perfo;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlanIO;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.punchio.PunchIO;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.ICancelTracker;

import jssc.SerialPortList;

/**
 * this object is responsible for driving the punch
 *
 * @author pfreydiere
 */
public class PunchProcess {

  private static Logger logger = Logger.getLogger(PunchProcess.class);

  private Config config;

  public PunchProcess(Config config) {
    assert config != null;
    this.config = config;
  }

  private File[] files = null;

  private void searchForFile(File folder, List<File> retrievedFiles) {

    logger.debug("entering " + folder);

    if (folder == null || !folder.isDirectory()) return;

    File[] elements =
        folder.listFiles(
            new FileFilter() {
              @Override
              public boolean accept(File pathname) {
                return pathname != null
                    && pathname.isFile()
                    && (pathname.getName().endsWith(".punch")
                        || pathname.getName().endsWith(".gcode"));
              }
            });

    retrievedFiles.addAll(Arrays.asList(elements));

    File[] subfolders =
        folder.listFiles(
            new FileFilter() {

              @Override
              public boolean accept(File pathname) {

                return pathname.isDirectory();
              }
            });

    if (subfolders != null) {
      for (File f : subfolders) {
        searchForFile(f, retrievedFiles);
      }
    }
  }

  public void searchForFile() {

    if (config.fileFolderPath == null) {
      this.files = new File[0];
      return;
    }

    assert config.fileFolderPath != null;
    ArrayList<File> a = new ArrayList<File>();

    searchForFile(config.fileFolderPath, a);
    this.files = a.toArray(new File[a.size()]);
  }

  public File[] getFiles() {
    return this.files;
  }

  Executor singleThreadExecutor;

  private CancelTracker cancelTracker;

  /**
   * start punch a file
   *
   * @param listener
   * @param filesToPunch
   * @throws Exception
   */
  public void startPunch(PunchListener listener, File[] filesToPunch) throws Exception {

    if (cancelTracker != null) {
      cancelTracker.cancel();
    }
    cancelTracker = null;

    // check files
    LinkedHashMap<File, PunchPlan> punchplan = new LinkedHashMap<>();

    ArrayList<String> messages = new ArrayList<>();

    for (File f : filesToPunch) {
      logger.debug("evaluate file :" + f);
      try {
        PunchPlan p = null;
        if (f.getName().endsWith(".punch")) {
          p = PunchIO.readPunchFile(f).getPunchplan();
        } else if (f.getName().endsWith(".gcode")) {
          p = PunchPlanIO.readFromGRBL(f);
        }

        punchplan.put(f, p);

      } catch (Exception ex) {
        messages.add("error in reading " + f);
        logger.error(ex.getMessage(), ex);
      }
    }

    final CancelTracker finalCancelTracker = new CancelTracker();
    this.cancelTracker = finalCancelTracker;

    ExecutorService exec = Executors.newSingleThreadExecutor();
    exec.submit(
        new Runnable() {
          @Override
          public void run() {
            // TODO Auto-generated method stub
            try {
              for (Entry<File, PunchPlan> e : punchplan.entrySet()) {

                File fileToProcess = e.getKey();
                if (listener != null) {
                  SwingUtilities.invokeLater(
                      () -> {
                        listener.startFile(fileToProcess);
                      });
                }

                backgroundThreadProcessPunchPlan(
                    fileToProcess, e.getValue(), listener, finalCancelTracker);

                if (listener != null) {
                  SwingUtilities.invokeLater(
                      () -> {
                        listener.finishedFile(fileToProcess);
                      });
                }
              }

            } catch (Exception ex) {
              // error
              logger.error("error in punching :" + ex.getMessage(), ex);
              if (listener != null) {
                SwingUtilities.invokeLater(
                    () -> {
                      listener.message("ERROR :" + ex.getMessage());
                    });
              }
            }
          }
        });
    this.singleThreadExecutor = exec;
  }

  private void backgroundThreadProcessPunchPlan(
      final File file, PunchPlan p, PunchListener listener, ICancelTracker cancelTracker)
      throws Exception {

    GRBLMachine machine = new GRBLMachine();
    GRBLMachineParameters params = new GRBLMachineParameters();
    logger.debug("existing com :");
    logger.debug(Arrays.asList(SerialPortList.getPortNames()));
    params.setComPort(config.usbPort);

    if (listener != null) {
      SwingUtilities.invokeLater(
          () -> {
            listener.initializing("opening machine communication");
            listener.informCurrentPunchPosition(file, 0, p.getCommandsByRef().size());
          });
    }

    MachineControl open = machine.open(params);
    try {
      open.setMachineControlListener(
          new MachineControlListener() {

            @Override
            public void statusChanged(MachineStatus status) {
              logger.debug("status changed :" + status);
            }

            @Override
            public void error(String message) {
              logger.debug("error :" + message);
              if (listener != null) {
                SwingUtilities.invokeLater(
                    () -> {
                      listener.message("ERROR :" + message);
                    });
              }
            }

            @Override
            public void currentMachinePosition(
                String status, final double wx, final double wy, double mx, double my) {
              logger.debug("status position :" + wx + "," + wy);
              if (listener != null) {
                SwingUtilities.invokeLater(
                    () -> {
                      listener.message(String.format("machine position %.2f %.2f ... ", wx, wy));
                    });
              }
            }
          });

      if (listener != null) {
     
        SwingUtilities.invokeLater(
            () -> {
              listener.initializing("Wait for the machine ... ");
            });
      }
      // wait for the initialization
      Thread.sleep(4000);

      // homing

      if (listener != null) {
        SwingUtilities.invokeLater(
            () -> {
              listener.initializing("initializing ... ");
            });
      }

      HomingCommand homing = new HomingCommand();
      open.sendCommand(homing);
      open.flushCommands();

      if (listener != null) {
        SwingUtilities.invokeLater(
            () -> {
              listener.initializing("initializing done ... ");
            });
      }

      List<Command> l = p.getCommandsByRef();
      // send commands, blocking procedure
      int cpt = 0;
      for (Command c : l) {
        try {
          if (cancelTracker.isCanceled()) {
            return;
          }
          open.sendCommand(c);
          // feedback
          if ((cpt % 20) == 0 && listener != null) {
            final int finalcpt = cpt;
            SwingUtilities.invokeLater(
                () -> {
                  listener.informCurrentPunchPosition(file, finalcpt, l.size());
                });
          }
          cpt++;
        } catch (Exception ex) {
          logger.error(ex.getMessage(), ex);
          throw new Exception(ex);
        }
      }
      
      if (listener != null) {
          // listener.initializing("initializing ... ");
          SwingUtilities.invokeLater(
              () -> {
                listener.initializing("End of Punch for " + file.getName());
              });
        }

    } finally {
      // switch off the machine
      open.close();
    }
  }

  /** stop method for aborting the process, */
  public void stop() {}
}