package org.barrelorgandiscovery.extensionsng.scanner.scan.trigger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.scanner.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.scan.IWebCamListener;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.tools.Disposable;

import com.github.sarxos.webcam.Webcam;

public class MachineTrigger extends Trigger implements Disposable {

  private static Logger logger = Logger.getLogger(MachineTrigger.class);

  private ICancelTracker cancelTracker = null;

  private AbstractMachineParameters parameters = null;

  private ExecutorService executor;

  public MachineTrigger(Webcam webcam,
		  IWebCamListener listener, 
		  PerfoScanFolder psf, 
		  AbstractMachineParameters parameters) {
    super(webcam, listener, psf);
    this.parameters = parameters;
  }

  @Override
  public void start() throws Exception {
    dispose();
    AbstractMachine machine = parameters.createAssociatedMachineInstance();
    MachineControl mc = machine.open(parameters);
    logger.debug("machine opened");//$NON-NLS-1$
    Thread.sleep(3000);
    logger.debug("homing ...");//$NON-NLS-1$
    mc.sendCommand(new HomingCommand());
    logger.debug("... homing done");//$NON-NLS-1$
    Thread.sleep(3000);
    logger.debug("homing ended");
    if (cancelTracker != null) {
      cancelTracker.cancel();
      cancelTracker = null;
    }
    this.cancelTracker = new CancelTracker();
    Runnable r =
        new Runnable() {
          @Override
          public void run() {
            try {
              double y = 0.0;
              while (!cancelTracker.isCanceled()) {
                y +=30;
                mc.sendCommand(new DisplacementCommand(y, 0));
              
                Thread.sleep(3000); // for hi res ...
                
                takePicture();
                logger.debug("picture taken");
              }
              logger.debug("advance ended"); //$NON-NLS-1$
            } catch (Throwable ex) {
              logger.error("error while scanning :" + ex.getMessage(), ex); //$NON-NLS-1$
              dispose();
            }
          }
        };
    executor = Executors.newSingleThreadExecutor();
    logger.debug("start advance");//$NON-NLS-1$
    executor.submit(r);
  }

  @Override
  public void dispose() {
	  logger.debug("dispose the machine trigger");
    if (cancelTracker != null) {
      cancelTracker.cancel();
      cancelTracker = null;
    }
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
    }
  }

  @Override
  public void stop() {
	  dispose();
  }
}
