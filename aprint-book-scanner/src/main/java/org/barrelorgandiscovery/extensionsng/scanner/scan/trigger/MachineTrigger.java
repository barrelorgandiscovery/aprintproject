package org.barrelorgandiscovery.extensionsng.scanner.scan.trigger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
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

	private MachineControl mc;

	public MachineTrigger(Webcam webcam, IWebCamListener listener, PerfoScanFolder psf,
			AbstractMachineParameters parameters, ITriggerFeedback triggerFeedback) {
		super(webcam, listener, psf, triggerFeedback);
		this.parameters = parameters;
	}

	@Override
	public void start() throws Exception {
		dispose();
		AbstractMachine machine = parameters.createAssociatedMachineInstance();

		giveFeedback("Opening machine with parameters");
		try {
			mc = machine.open(parameters);
			logger.debug("machine opened");//$NON-NLS-1$
			giveFeedback("Machine Opened !, well sending homing command");
			Thread.sleep(3000);
			logger.debug("homing ...");//$NON-NLS-1$
			mc.sendCommand(new HomingCommand());
			giveFeedback("Homing sent");
			logger.debug("... homing done");//$NON-NLS-1$
			Thread.sleep(3000);

			MachineStatus machineStatus = mc.getStatus();
			if (!(machineStatus == MachineStatus.IDLE)) {
				giveFeedback("Machine seems not be ready, please check it before going further ... ");
				throw new Exception("bad machine state");
			}

			logger.debug("homing ended");
			if (cancelTracker != null) {
				cancelTracker.cancel();
				cancelTracker = null;
			}
			this.cancelTracker = new CancelTracker();

			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						double y = 0.0;
						while (!cancelTracker.isCanceled()) {
							final double finaly = y;
							SwingUtilities.invokeAndWait(() -> {
								giveFeedback("Moving to " + finaly);
							});
							y += 30;
							mc.sendCommand(new DisplacementCommand(y, 0));

							Thread.sleep(200); // for hi res ...

							MachineStatus machineRunningStatus;
							do {
								machineRunningStatus = mc.getStatus();
								Thread.sleep(100);
							} while (machineRunningStatus != MachineStatus.IDLE);

							SwingUtilities.invokeAndWait(() -> {
								giveFeedback("take picture");
							});

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
		} catch (Throwable t) {
			logger.error("Error, " + t.getMessage(), t);
			giveFeedback("ERROR : " + t.getMessage());
		}
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

		try {
			if (mc != null) {
				mc.close();
				this.mc = null;
			}
		} catch (Throwable t) {
			logger.debug(t.getMessage(), t);
		}
	}

	@Override
	public void stop() {
		dispose();
	}
}
