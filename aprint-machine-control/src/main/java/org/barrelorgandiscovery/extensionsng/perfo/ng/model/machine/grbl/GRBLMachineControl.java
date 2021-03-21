package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineDirectControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GCodeCompiler;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.ISerialPort;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;

import jssc.SerialPort;
import jssc.SerialPortEvent;

/**
 * Communicator with the GRBL middleware
 * 
 * @author pfreydiere
 * 
 */
class GRBLMachineControl implements MachineControl, MachineDirectControl {

	private static final String ALARM_RECEIVED_STATUS = "Alarm:";

	// beware the status is done every 500 ms
	private static final int TRIGGERED_STATUS_TIMER = 100;

	private static Logger logger = Logger.getLogger(GRBLMachineControl.class);

	ISerialPort serialPort;

	private GRBLProtocolState grblProtocolState;

	private ScheduledExecutorService status;

	// don't go to zero, otherwise grbl strip commands
	// in regulation mode
	public static int COMMANDS_IN_BUFFER_OBJECTIVE = 1;

	public static int MAX_ACTIVE = 5;
	
	public static int MAX_BUFFER = 10;

	// used for debug
	static Class serialPortClass = org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPort.class;

	/**
	 * semaphore for command sending a command, block if there is too much command
	 * sent at a time
	 */
	// private Semaphore commandSendLockSem;

	private final LinkedBlockingDeque<String> commandBuffer; // Manually specified commands

	private final LinkedBlockingDeque<String> activeCommandList; // Currently running commands

	private long lastMachineStatusTime = 0;
	
	private AtomicLong userFeedback = new AtomicLong();

	private volatile MachineStatus lastMachineStatus = MachineStatus.UNKNOWN;

	PortReader listener2 = new PortReader();

	enum MachineStatus {
		UNKNOWN, ALARM, RUNNING, IDLE, ERROR
	}

	private String currentPortName;
	private GCodeCompiler commandCompiler;

	@Override
	public org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus getStatus() {

		MachineStatus finalLastMachineStatus = lastMachineStatus;
		if (finalLastMachineStatus == null) {
			return null;
		}

		switch (finalLastMachineStatus) {
		case ALARM:
			return org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus.ERROR;
		case RUNNING:
			return org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus.RUNNING;
		case IDLE:
			return org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus.IDLE;
		case ERROR:
			return org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus.ERROR;
		case UNKNOWN:
			return org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus.UNKNOWN;
		}

		return org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus.UNKNOWN;

	}

	private class PortReader implements
			org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortEventListener {

		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.isRXCHAR() && event.getEventValue() > 0) {
				try {
					String receivedData;

					receivedData = serialPort.readString(event.getEventValue());

					logger.debug("received data from grbl machine :" + receivedData);
					machineInteractionLog.add(new TimestampedMachineInteraction(System.nanoTime(), true, receivedData));
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

		commandBuffer = new LinkedBlockingDeque<>();
		activeCommandList = new LinkedBlockingDeque<>();

		init(portName); // raise exception in case of issue
		currentPortName = portName;
		assert commandCompiler != null;
		this.commandCompiler = commandCompiler;

		logger.debug("schedule status watchdog");
		status = Executors.newSingleThreadScheduledExecutor();

		status.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					if (grblProtocolState != null) {
						grblProtocolState.sendStatusRequest();
					}

					// transaction
					MachineControlListener v = userMachineControlListener;
					if (v != null) {
						// pulling elements
						try {
							// pump messages
							TimestampedMachineInteraction s = machineInteractionLog.poll();
							while (s != null) {
								if (s.isIn) {
									v.rawElementReceived(s.command);
								} else {
									v.rawElementSent(s.command);
								}
								s = machineInteractionLog.poll();
							}
						} catch (Throwable t) {
							logger.debug(t.getMessage(), t);
						}

					}

				} catch (Throwable ex) {
					logger.error("error sending status command :" + ex.getMessage(), ex);
				}
			}
		}, 50 * TRIGGERED_STATUS_TIMER, TRIGGERED_STATUS_TIMER, TimeUnit.MILLISECONDS);
	}

	private void init(String portName) throws Exception {

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

		commandBuffer.clear();
		activeCommandList.clear();

		lastMachineStatus = MachineStatus.UNKNOWN;

		// define state
		grblProtocolState =

				new GRBLProtocolState(new SendString() {
					@Override
					public void sendString(String stringToSend) throws Exception {
						if (stringToSend != null) {
							machineInteractionLog
									.add(new TimestampedMachineInteraction(System.nanoTime(), false, stringToSend));
						}
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
							logger.error("ALARM RECEIVED FROM ARDUINO :" + line);

						} else if (line.startsWith("error: Unsupported command")) {
							logger.error("COMMAND IS NOT SUPPORTED");
							logger.debug("release command queue");
							activeCommandList.pop();
						}

						if (userMachineControlListener != null) {
							try {
								userMachineControlListener.informationReceived(line);
							} catch (Throwable t) {
								logger.error("error in user information received " + t.getMessage(), t);
							}
						}

					}

					@Override
					public void resetted() {
						// if resetted, unlock all sended commands
						// there may still have pipelined commands

						// reinit
						commandBuffer.clear();
						activeCommandList.clear();
					}

					@Override
					public void statusReceived(GRBLStatus status) {
						
						long feedbackcount = userFeedback.incrementAndGet();

						if (userMachineControlListener != null && status != null && status.workingPosition != null
								&& (feedbackcount % 5 == 0)
								) {
							try {
								userMachineControlListener.currentMachinePosition(status.status,
										status.workingPosition.x, status.workingPosition.y);
							} catch (Throwable t) {
								logger.error("error in currentMachinePosition handler " + t.getMessage(), t);
							}
						}

						assert status != null;

						if (status.bufferSize != null) {
							// buffer size is the occupied slots

							synchronized (activeCommandList) {
								while ((!commandBuffer.isEmpty())  && activeCommandList.size() < MAX_ACTIVE 
										&& (MAX_ACTIVE - status.bufferSize) 
										- activeCommandList.size() > 0) {

									// push commands from buffer
									synchronized (commandBuffer) {
										String command = commandBuffer.peek();
										try {

											logger.debug("call send command");
											grblProtocolState.sendCommand(command);
											commandBuffer.pop();
											activeCommandList.add(command);
											commandBuffer.notify();

										} catch (Exception ex) {
											logger.error("error in sending the command " + ex.getMessage(), ex);
										}
									}
								}
							}

						} else if (status.availableCommandsInPlannedBuffer != null) {

							if (status.availableCommandsInPlannedBuffer == 0) {
								logger.error("error buffer full");
							}

							synchronized (activeCommandList) {
								logger.debug("active commands :" + activeCommandList.size());
								logger.debug("command Buffer list :" + commandBuffer.size());
								
								while ((!commandBuffer.isEmpty()) && activeCommandList.size() < MAX_ACTIVE 
										&& status.availableCommandsInPlannedBuffer
										- COMMANDS_IN_BUFFER_OBJECTIVE - activeCommandList.size() > 0) {

									// push commands from buffer
									synchronized (commandBuffer) {
										String command = commandBuffer.peek();
										try {

											logger.debug("call send command " + command);
											grblProtocolState.sendCommand(command);
											commandBuffer.pop();
											activeCommandList.add(command);
											commandBuffer.notify();

										} catch (Exception ex) {
											logger.error("error in sending the command " + ex.getMessage(), ex);
										}
									}
								}
							}

						} else {
							
							pumpCommands();

						}

						if (status != null) {

							// decode machine status

							MachineStatus current = MachineStatus.UNKNOWN;

							if ("Run".equalsIgnoreCase(status.status)) {
								current = MachineStatus.RUNNING;
							} else if ("Idle".equalsIgnoreCase(status.status)) {
								current = MachineStatus.IDLE;
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

						// depending if we are managed or not,
						// release the command Queue

						if (!activeCommandList.isEmpty()) {
							activeCommandList.pop();
						}
						
					
						

						logger.debug("available in command queue after command ack :" + activeCommandList.size());
					}

					@Override
					public void welcome(String welcomeString) {
						logger.info("Welcome arrived :" + welcomeString);
					}
				});

		/**
		 * start init
		 */

		// this trick is done for testing purpose
		Constructor serialPortConstructor = serialPortClass.getConstructor(new Class[] { String.class });

		serialPort = (ISerialPort) serialPortConstructor.newInstance(portName);

		logger.debug("opening " + portName);

		serialPort.openPort();

		// open the port

		logger.debug("port opened");

		logger.debug("setting communication parameters");
		serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);

		serialPort.addEventListener(listener2, SerialPort.MASK_RXCHAR);
	}

	private MachineControlListener userMachineControlListener;

	public static class TimestampedMachineInteraction {
		public final long timestamp;
		/**
		 * true if the command is received from machine
		 */
		public final boolean isIn;
		public final String command;

		public TimestampedMachineInteraction(long timestamp, boolean isIn, String command) {
			this.timestamp = timestamp;
			this.isIn = isIn;
			this.command = command;
		}
	}

	// reported sent commands
	private ConcurrentLinkedQueue<TimestampedMachineInteraction> machineInteractionLog = new ConcurrentLinkedQueue<>();

	@Override
	public void setMachineControlListener(MachineControlListener listener) {
		this.userMachineControlListener = listener;
	}

	private void checkState() throws Exception {
		if (serialPort == null)
			throw new Exception("invalid state");
	}

	@Override
	public void close() throws Exception {
		if (serialPort != null) {
			try {
				serialPort.closePort();
			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
			}
		}
		
		serialPort = null;
		grblProtocolState = null;
	}

	@Override
	public void sendCommand(Command command) throws Exception {
		checkState();

		command.accept(0, commandCompiler);

		List<String> commands = commandCompiler.getGCODECommands();
		while (commands.size() > 0) {
			// pop commands, and send them
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

		this.commandCompiler.reset();
	}

	/**
	 * this method send a string command to the grbl, this is a blocking command
	 * 
	 * @param cmd
	 * @throws Exception
	 */
	private void sendOneCommand(String cmd) throws Exception {

		logger.debug("entered command lock");
		synchronized (commandBuffer) {

			if (commandBuffer.size() > MAX_BUFFER) {
				// block
				logger.debug("blocking thread for sending command :" + commandBuffer.size());
				commandBuffer.wait();
				logger.debug("done blocking");
			}
			logger.debug("adding command in buffer");
			commandBuffer.add(cmd);
		}
		logger.debug("passed the command lock");

	}

	@Override
	public void prepareForWork() throws Exception {
		List<String> preludeCommands = commandCompiler.getPreludeCommands();
		if (preludeCommands != null) {
			logger.debug("sending prelude command");
			for (String s : preludeCommands) {
				sendOneCommand(s);
			}
		}
	}

	@Override
	public void endingForWork() throws Exception {
		List<String> endingCommands = commandCompiler.getEndingCommands();
		if (endingCommands != null) {
			logger.debug("sending ending command");
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
			Thread.sleep(100);
		}

		logger.debug("machine is not running");
	}

	private void statusChanged(MachineStatus newStatus) {
		logger.debug("new machine status :" + newStatus);
	}

	@Override
	public void directCommand(String command) throws Exception {
		if (command != null && !command.isEmpty()) {
			logger.debug("send command " + command);
			this.sendOneCommand(command);
		}
	}

	private void pumpCommands() {
		// assert status.bufferSize == null;
		// assert status.availableCommandsInPlannedBuffer == null;
		synchronized (activeCommandList) {
			synchronized (commandBuffer) {
				while (activeCommandList.size() < MAX_ACTIVE && !commandBuffer.isEmpty()) {
					
					String command = commandBuffer.peek();
					try {
						logger.debug("call send command");
						grblProtocolState.sendCommand(command);
						commandBuffer.pop();
						activeCommandList.add(command);
						commandBuffer.notify();

					} catch (Exception ex) {
						logger.error("error in sending the command " + ex.getMessage(), ex);
					}
				}
			}
		}
	}

}
