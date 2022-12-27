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
 * create a stream for sending commands, this class handle normal CNC command
 * streams, and also pause timer
 * 
 * @author pfreydiere
 * 
 */
public class MachineCommandStream {

	private static final Logger logger = Logger.getLogger(MachineCommandStream.class);

	public static final int STATE_PROCESSING = 0;
	public static final int STATE_PAUSED = 1;

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

		/**
		 * error while processing
		 * 
		 * @param ex
		 */
		void errorInProcessing(Exception ex);

		/**
		 * inform about the current stream state (used for gui)
		 * 
		 * @param currentStreamState the current stream state
		 */
		void currentStreamState(int currentStreamState);
	}

	/**
	 * the machine command handling
	 */
	private MachineControl machineControl;

	/**
	 * the current punch plan
	 */
	private PunchPlan punchPlan;

	/**
	 * processing thread
	 */
	private Thread processingThread = null;

	/**
	 * feedback on streaming
	 */
	private StreamingProcessingListener listener = null;

	/**
	 * configured Pause state for machines
	 */
	// may be null if no pause time state
	private PauseTimerState pauseTimerState = null;

	public MachineCommandStream(MachineControl machineControl, PunchPlan punchPlan,
			StreamingProcessingListener listener, PauseTimerState pauseTimerState) throws Exception {
		
		
		assert machineControl != null;
		this.machineControl = machineControl;
		assert punchPlan != null;
		this.punchPlan = punchPlan;

		assert listener != null;
		this.listener = listener;
		
		this.pauseTimerState = pauseTimerState;

	}

	/**
	 * start sending the commands from index index
	 * 
	 * @param index
	 * @throws Exception
	 */
	public void startStreamFrom(final int index) throws Exception {

		// index check

		if (listener != null) {
			try {
				listener.currentStreamState(STATE_PROCESSING);
			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
			}
		}
		

		assert index < punchPlan.getCommandsByRef().size();
		assert index >= 0;

		Thread t = new Thread() {
			public void run() {

				try {
					
					if (pauseTimerState != null) {
						pauseTimerState.startPunch();
					}
					
					final int j = index;
					List<Command> commands = punchPlan.getCommandsByRef();

					machineControl.prepareForWork();
					try {

						for (int i = j; i < commands.size(); i++) {
							try {

								Command cmd = commands.get(i);
								logger.debug("stream command sent :" + cmd);

								// hack for testing
								if (System.getProperty("fakeM100") != null) {
									if (cmd instanceof PunchCommand) {
										XYCommand c = (XYCommand) cmd;
										cmd = new DisplacementCommand(c.getX(), c.getY());
									}
								}

								// and hack
								if (logger.isDebugEnabled()) {
									logger.debug("command stream send command :" + cmd);
								}

								if (machineControl.getStatus() == MachineStatus.ERROR) {
									throw new Exception("Machine has been returned an alarm");
								}

								machineControl.sendCommand(cmd);

								if (pauseTimerState != null) {
									boolean enter = false;
									while (pauseTimerState.isInPause()) {
										if (!enter) {
											if (listener != null) {
												try {
													listener.currentStreamState(STATE_PAUSED);
												} catch (Throwable t) {
													logger.error(t.getMessage(), t);
												}
											}
										}
										enter = true;

										Thread.sleep(1000);
									}
									if (enter) {
										// signal the processing
										if (listener != null) {
											try {
												listener.currentStreamState(STATE_PROCESSING);
											} catch (Throwable t) {
												logger.error(t.getMessage(), t);
											}
										}
									}
								}

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
												"error has been encountered on punching :" + ft.getMessage(), ft)); 
									}

								});
								// raise
								throw t;
							}

						}

					} finally {
						// in case we must send specific commands on stop,
						// for example, disable laser
						machineControl.endingForWork();

						if (pauseTimerState != null) {
							pauseTimerState.startPunch();
						}


						processingThread = null;
						if (listener != null) {
							listener.allCommandsEnded();
						}
					}

				} catch (Exception ex) {
					if (!(ex instanceof InterruptedException)) {
						logger.error("Error while processing commands :" + ex.getMessage(), ex);
						listener.errorInProcessing(ex);
					}
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

	public void setPauseTimerState(PauseTimerState pauseTimerState) {
		this.pauseTimerState = pauseTimerState;
	}
	
	public PauseTimerState getPauseTimerState() {
		return pauseTimerState;
	}
	
}
