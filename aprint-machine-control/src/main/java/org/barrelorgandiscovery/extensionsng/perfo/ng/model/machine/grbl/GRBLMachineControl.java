package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GCodeCompiler;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Communicator with the GRBL middleware
 * 
 * @author pfreydiere
 * 
 */
class GRBLMachineControl implements MachineControl {

	private static final String ALARM_RECEIVED_STATUS = "Alarm:";

	private static final int MAX_PERMIT_SEND = 4;

	// beware the status is done every 500 ms
	private static final int TRIGGERED_STATUS_TIMER = 500;

	private static Logger logger = Logger.getLogger(GRBLMachineControl.class);

	private SerialPort serialPort;

	private GRBLProtocolState grblProtocolState;

	private ScheduledExecutorService status;

	/**
	 * semaphore for command sending a command, block if there is too much command
	 * sent at a time
	 */
	private Semaphore commandSendLockSem;

	private static final int MAX_QUEUE = 10;

	/**
	 * semaphore for command in the pipeline queue, block if the grbl buffer is full
	 */
	private Semaphore commandQueue;

	private long lastMachineStatusTime = 0;
	private MachineStatus lastMachineStatus = MachineStatus.UNKNOWN;

	enum MachineStatus {
		UNKNOWN, ALARM, RUNNING, IDLE_OR_ERROR
	}

	private class PortReader implements SerialPortEventListener {

		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.isRXCHAR() && event.getEventValue() > 0) {
				try {
					String receivedData;

					receivedData = serialPort.readString(event.getEventValue());

					logger.debug("received data from grbl machine :" + receivedData);
					grblProtocolState.received(receivedData);

				} catch (Exception ex) {
					System.err.println("Error in receiving string from COM-port: " + ex);
					ex.printStackTrace();
				}
			}
		}

	}

	/**
	 * constructor of the machine control panel
	 * 
	 * @param portName the port name
	 * 
	 * @throws Exception
	 */
	public GRBLMachineControl(String portName, GCodeCompiler commandCompiler) throws Exception {

		init(portName); // raise exception in case of issue
		currentPortName = portName;
		assert commandCompiler != null;
		this.commandCompiler = commandCompiler;
	}

	private String currentPortName;
	private GCodeCompiler commandCompiler;

	private void init(String portName) throws SerialPortException {

		/**
		 * reinit
		 */
		if (serialPort != null) {
			try {
				serialPort.closePort();
			} catch (Exception ex) {
				logger.error("fail in closing port :" + ex.getMessage(), ex);
			}
		}

		commandSendLockSem = new Semaphore(MAX_PERMIT_SEND, true);
		commandQueue = new Semaphore(MAX_QUEUE, true);

		lastMachineStatus = MachineStatus.UNKNOWN;

		/**
		 * start init
		 */

		serialPort = new SerialPort(portName);

		logger.debug("opening " + portName);

		serialPort.openPort();

		// open the port

		logger.debug("port opened");

		logger.debug("setting communication parameters");
		serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);

		serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);

		grblProtocolState = new GRBLProtocolState(new SendString() {
			@Override
			public void sendString(String stringToSend) throws Exception {

				serialPort.writeString(stringToSend);

			}
		}, new GRBLProtocolStateListener() {

			@Override
			public void unknownReceived(String line) {

				logger.debug("unknown Received :" + line);

				if (line == null || line.length() == 0)
					return;

				assert line != null && line.length() > 0;

				if (line.startsWith(ALARM_RECEIVED_STATUS)) { // "ALARM: Hard
																// limit"
					// must do a RESET to Restart
					logger.error("ALARM RECEIVED FROM ARDUINO");
				} else if (line.startsWith("error: Unsupported command")) {
					logger.error("COMMAND IS NOT SUPPORTED");
					commandQueue.release();
				}

				logger.debug("release command sem");
				commandSendLockSem.release();
				logger.debug("command sem released");

			}

			@Override
			public void resetted() {
				// if resetted, unlock all sended commands
				// there may still have pipelined commands

				commandSendLockSem.release(100);
				commandSendLockSem = new Semaphore(MAX_PERMIT_SEND);

				commandQueue.release(MAX_QUEUE);
				commandQueue = new Semaphore(MAX_QUEUE, true);

			}

			@Override
			public void statusReceived(GRBLStatus status) {

				if (listener != null && status != null && status.machinePosition != null) {
					// logger.debug("status received :" + status);
					listener.currentMachinePosition(status.status, status.workingPosition.x, status.workingPosition.y,
							status.machinePosition.x, status.machinePosition.y);
				}

				assert status != null;

				if (status.bufferSize != null) {

					int permits = commandQueue.availablePermits();
					int delta = (MAX_QUEUE - status.bufferSize) - permits; // used buffer

					if (delta != 0) {
						if (delta > 0) {
							//
							logger.debug("readjust command Queue from delta " + delta);
							for (int i = 0; i < delta; i++) {
								commandQueue.release();
							}

						}
					}

				} else {
					// status.bufferSize == null

					// no status come from the device, so release depending on the status
					if (commandQueue.availablePermits() < 1) {
						logger.debug("no commands regulation");
						commandQueue.release();
					}
				}

				if (status != null) {

					// decode machine status

					MachineStatus current = MachineStatus.UNKNOWN;

					if ("Run".equalsIgnoreCase(status.status)) {
						current = MachineStatus.RUNNING;
					} else if ("Idle".equalsIgnoreCase(status.status)) {
						current = MachineStatus.IDLE_OR_ERROR;
					} else if ("Alarm".equalsIgnoreCase(status.status)) {
						current = MachineStatus.ALARM;
					} else {
						current = MachineStatus.UNKNOWN;
					}

					if (lastMachineStatus != current) {

						try {
							statusChanged(current);
						} catch (Exception ex) {
							logger.error("error in changing status " + ex.getMessage(), ex);
						}

						lastMachineStatus = current;

					}

				} // null status
				lastMachineStatusTime = System.currentTimeMillis();

			}

			@Override
			public void commandAck() {
				logger.debug("command ack received");
				// notify
				// synchronized (commandSendLockSem) {
				// try {
				// unblock one command
				logger.debug("release ... ");
				commandSendLockSem.release();
				// commandQueue.release();
				logger.debug("release .. done");
				// } catch (Throwable t) {
				// logger.error("" + t.getMessage(), t);
				// }
				// }

			}
		});

		logger.debug("schedule status watchdog");
		status = Executors.newSingleThreadScheduledExecutor();
		status.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					if (grblProtocolState != null) {
						grblProtocolState.sendStatusRequest();
					}
				} catch (Exception ex) {
					logger.error("error sending status command :" + ex.getMessage(), ex);
				}
			}
		}, TRIGGERED_STATUS_TIMER, TRIGGERED_STATUS_TIMER, TimeUnit.MILLISECONDS);
	}

	private MachineControlListener listener;

	@Override
	public void setMachineControlListener(MachineControlListener listener) {
		this.listener = listener;
	}

	private void checkState() throws Exception {
		if (serialPort == null)
			throw new Exception("invalid state");
	}

	@Override
	public void close() throws Exception {
		serialPort.closePort();
		serialPort = null;
		grblProtocolState = null;
	}

	@Override
	public void sendCommand(Command command) throws Exception {
		checkState();

		this.commandCompiler.reset();
		
		command.accept(0, commandCompiler);

		List<String> commands = commandCompiler.getGCODECommands();
		while (commands.size() > 0) {
			String s = commands.remove(0);
			logger.debug("sending :" + s);
			sendOneCommand(s);
			logger.debug("command sent");
		}
	}

	@Override
	public void reset() throws Exception {

		logger.debug("reinit the machine state");
		init(currentPortName);
		logger.debug("done !");
	}

	/**
	 * this method send a string command to the grbl
	 * 
	 * @param cmd
	 * @throws Exception
	 */
	private void sendOneCommand(String cmd) throws Exception {

		logger.debug("call send command");
		grblProtocolState.sendCommand(cmd);

		logger.debug("entered command lock");

		commandSendLockSem.acquire();

		commandQueue.acquire();

		logger.debug("available in command send lock :" + commandSendLockSem.availablePermits());
		logger.debug("available in command queue :" + commandQueue.availablePermits());

		logger.debug("passed the command lock");

	}
	
	@Override
	public void prepareForWork() throws Exception {
		List<String> preludeCommands = commandCompiler.getPreludeCommands();
		if (preludeCommands != null) {
			for (String s : preludeCommands) {
				sendOneCommand(s);
			}
		}
	}

	@Override
	public void endingForWork() throws Exception {
		List<String> endingCommands = commandCompiler.getPreludeCommands();
		if (endingCommands != null) {
			for (String s : endingCommands) {
				sendOneCommand(s);
			}
		}	
	}
	
	/**
	 * wait for all commands sent, this method wait for the non running status of
	 * the
	 */
	public void flushCommands() throws Exception {

		long time = System.currentTimeMillis();
		while (lastMachineStatusTime < time) {
			Thread.sleep(100);
		}

		while (lastMachineStatus == MachineStatus.RUNNING) {
			// logger.debug("wait for machine is ready");
			Thread.sleep(100);
		}

		logger.debug("machine is not running");
	}

	private void statusChanged(MachineStatus newStatus) {
		logger.debug("new machine status :" + newStatus);
	}

}
