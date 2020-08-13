package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.XYCommand;
import org.barrelorgandiscovery.tools.JMessageBox;

/**
 * create a stream for sending commands
 * 
 * @author pfreydiere
 * 
 */
public class MachineCommandStream {

	public interface StreamingProcessingListener {
		/**
		 * signal the command at index 'index' has been processed
		 * 
		 * @param index
		 */
		void commandProcessed(int index);

		/**
		 * end of stream
		 */
		void allCommandsEnded();
	}

	private static final Logger logger = Logger
			.getLogger(MachineCommandStream.class);

	private MachineControl machineControl;

	private PunchPlan punchPlan;

	private Thread processingThread = null;

	private StreamingProcessingListener listener = null;

	public MachineCommandStream(MachineControl machineControl,
			PunchPlan punchPlan, StreamingProcessingListener listener)
			throws Exception {
		assert machineControl != null;
		this.machineControl = machineControl;
		assert punchPlan != null;
		this.punchPlan = punchPlan;

		assert listener != null;
		this.listener = listener;

	}

	/**
	 * start sending the commands from index index
	 * 
	 * @param index
	 * @throws Exception
	 */
	public void startStreamFrom(final int index) throws Exception {

		// index check

		assert index < punchPlan.getCommandsByRef().size();
		assert index >= 0;

		Thread t = new Thread() {
			public void run() {

				try {
					final int j = index;
					List<Command> commands = punchPlan.getCommandsByRef();
					for (int i = j; i < commands.size(); i++) {
						try {

							Command cmd = commands.get(i);
							logger.debug("stream command sent :" + cmd);

							// hack for testing
							if (System.getProperty("fakeM100") != null) {
								if (cmd instanceof PunchCommand) {
									XYCommand c = (XYCommand) cmd;
									cmd = new DisplacementCommand(c.getX(),
											c.getY());
								}
							}
							
							// and hack
							if (logger.isDebugEnabled()) {
								logger.debug("command stream send command :"
										+ cmd);
							}
							
							
							machineControl.sendCommand(cmd);
							
							StreamingProcessingListener l = listener;
							if (l != null) {
								logger.debug("command processed");
								l.commandProcessed(i);
							}

						} catch (InterruptedException interrupt) {
							// nothing to report
						} catch (RuntimeException t) {

							final Throwable ft = t;
							logger.error(t.getMessage(), t);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									JMessageBox.showError(null, new Exception(
											"une erreur a été rencontrée lors du perçage :"
													+ ft.getMessage(), ft));
								}

							});

							throw t;
						}

					}

					processingThread = null;
					if (listener != null) {
						listener.allCommandsEnded();
					}

				} catch (Exception ex) {
					logger.error(
							"Error while processing commands :"
									+ ex.getMessage(), ex);
				}

				logger.info("end of command processing");

			};
		};

		synchronized (this) {
			this.processingThread = t;
			t.start();
			
		}

	}

	/**
	 * Stop the stream
	 * 
	 * @throws Exception
	 */
	public void stopStreaming() throws Exception {

		if (processingThread != null) {
			try {
				processingThread.stop();
			} catch (Throwable t) {
			}
			this.processingThread = null;
		}

	}

	/**
	 * return is the stream is processing the commands
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return processingThread != null;
	}

	// //////////////////////////////////////////////////////////////////
	// external methods called for informing the machine state

	/**
	 * external signal for telling the previous command has been acknownledged
	 */
	public void commandAck() {

		if (!isRunning()) {
			return;
		}

	}

}
