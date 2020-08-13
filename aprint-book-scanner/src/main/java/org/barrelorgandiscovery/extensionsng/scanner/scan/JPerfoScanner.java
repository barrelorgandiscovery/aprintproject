package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLPunchMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLPunchMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.scanner.PerfoScanFolder;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.tools.Disposable;

import com.github.sarxos.webcam.Webcam;

import jssc.SerialPortList;

/**
 * Scanning panel
 * 
 * @author pfreydiere
 *
 */
public class JPerfoScanner extends JPanel implements Disposable {

  /** */
  private static final long serialVersionUID = 5453676730276044987L;

  private static Logger logger = Logger.getLogger(JPerfoScanner.class);

  private MachineControl machineControl;

  private PerfoScanFolder perfoScan;

  private final JLabel labelImage = new JLabel();

  private AtomicBoolean onlyWebCam = new AtomicBoolean(true);

  private ICancelTracker previewCancelTracker = new CancelTracker();

  /**
   * this class execute the bookscan
   *
   * @author use
   */
  class ScanBook implements Runnable {

    private ICancelTracker cancelTracker;
    private Webcam webCam;

    public ScanBook(ICancelTracker cancelTracker, Webcam webCam) {
      this.cancelTracker = cancelTracker;
      this.webCam = webCam;
    }

    @Override
    public void run() {

      double bookdisplacement = 0.0;

      while (!cancelTracker.isCanceled()) {
        try {
          logger.debug("take picture");//$NON-NLS-1$
          final BufferedImage picture = webCam.getDevice().getImage();
          try {
        	  // display image
            SwingUtilities.invokeAndWait(
                () -> {
                  labelImage.setIcon(new ImageIcon(picture));
                  JPerfoScanner.this.repaint();
                });
          } catch (Exception ex) {
            logger.error("error while showing the image " + ex.getMessage(), ex);//$NON-NLS-1$
          }

          if (!onlyWebCam.get()) {
        	  // save the image only in record mode
            logger.debug("save and move");//$NON-NLS-1$
            try {
              perfoScan.addNewImage(picture);
              machineControl.sendCommand(new DisplacementCommand(bookdisplacement, 0.0));
              Thread.sleep(2000);
              // machineControl.flushCommands();
            } catch (Exception ex) {
              logger.error(ex.getMessage(), ex);
            }
            bookdisplacement += 20.0;
            try {
              Thread.sleep(1000);
            } catch (Exception ex) {

            }
          }
        } catch (Exception _ex) {
          _ex.printStackTrace();
        }
      }

      if (webCam != null) {
        webCam.close();
        webCam = null;
      }
    }
  }

  ExecutorService exec = Executors.newSingleThreadExecutor();
  CancelTracker cancelTracker = new CancelTracker();

  Webcam defaultWebCam;

  public JPerfoScanner(MachineControl machineControl, PerfoScanFolder perfoScan) throws Exception {
    // assert machineControl != null;
    this.machineControl = machineControl;
    this.perfoScan = perfoScan;
    initComponents();
  }

  protected void initComponents() throws Exception {

    labelImage.setPreferredSize(new Dimension(700, 500));

    setLayout(new BorderLayout());
    add(labelImage, BorderLayout.CENTER);

    JToggleButton tb = new JToggleButton();
    tb.setText("Record");
    tb.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            JToggleButton t = (JToggleButton) e.getSource();
            if (t.getModel().isSelected()) {
              tb.setText("Record");
              onlyWebCam.set(false);
            } else {
              tb.setText("Stop Record");
              onlyWebCam.set(true);
            }
          }
        });

    final JButton btn = new JButton("Start");
    btn.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            try {

              if (!cancelTracker.isCanceled()) {
                cancelTracker.cancel();
                btn.setText("Start");
                defaultWebCam = null;
                return;
              }

              btn.setText("Stop");
              logger.debug("open web cam");//$NON-NLS-1$
              defaultWebCam = openWebCam();

              cancelTracker = new CancelTracker();
              logger.debug("start scan");//$NON-NLS-1$
              ScanBook taskToRun = new ScanBook(cancelTracker, defaultWebCam);

              logger.debug("start job");//$NON-NLS-1$
              exec.submit(taskToRun);

            } catch (Exception ex) {
              logger.error("error in scan :" + ex.getMessage(), ex);//$NON-NLS-1$
            }
          }

          private Webcam openWebCam() {
            // open webcam
            Webcam defaultWebCam = Webcam.getWebcams().get(1);
            //  defaultWebCam.setViewSize(new Dimension(800,600));
            Dimension[] sizes = defaultWebCam.getViewSizes();
            System.out.println(Arrays.asList(sizes));
            defaultWebCam.setViewSize(sizes[sizes.length - 1]);
            defaultWebCam.open();
            return defaultWebCam;
          }
        });

    JPanel btnPanel = new JPanel();
    btnPanel.add(btn);
    btnPanel.add(tb);
    add(btnPanel, BorderLayout.SOUTH);

    cancelTracker.cancel();
  }

  @Override
  public void dispose() {
    exec.shutdownNow();
  }

  /**
   * main test scanner
   *
   * @param args
   */
  public static void main(String[] args) throws Exception {

    JFrame f = new JFrame();

    SwingUtilities.invokeAndWait(
        () -> {
          BasicConfigurator.configure(new LF5Appender());
        });

    GRBLPunchMachine machine = new GRBLPunchMachine();
    GRBLPunchMachineParameters params = new GRBLPunchMachineParameters();
    System.out.println("Available Port List :" + Arrays.asList(SerialPortList.getPortNames()));//$NON-NLS-1$
    params.setComPort("COM5");//$NON-NLS-1$
    MachineControl open = machine.open(params);

    Thread.sleep(4000);

    open.sendCommand(new HomingCommand());

    File perfoImageFolder = new File("c:\\temp\\perfo20180903");//$NON-NLS-1$
    if (!perfoImageFolder.exists()) {
      perfoImageFolder.mkdirs();
    }
    PerfoScanFolder psf = new PerfoScanFolder(perfoImageFolder);

    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(new JPerfoScanner(open, psf), BorderLayout.CENTER);

    f.setSize(600, 400);

    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
  }
}
