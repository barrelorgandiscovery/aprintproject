package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import org.apache.log4j.Logger;

/**
 * protocol handling, and GRBL State Handling
 * 
 * @author pfreydiere
 * 
 */
public class GRBLProtocolState {

	private static Logger logger = Logger.getLogger(GRBLProtocolState.class);

	enum GRBLState {
		UNKNOWN, ALARM, INITED, READY, WAITING_COMMAND_ACK, ERROR
	}

	private GRBLState currentState = GRBLState.UNKNOWN;

	private SendString sendString;

	private GRBLProtocolStateListener listener;

	public GRBLProtocolState(SendString sendString,
			GRBLProtocolStateListener listener) {
		assert sendString != null;
		this.sendString = sendString;
		this.listener = listener;
	}

	// current reading buffer
	StringBuilder buffer = new StringBuilder();

	ParsingState currentParsingState = ParsingState.NOCONTEXT;

	enum ParsingState {
		NOCONTEXT, READING, PARSINGSTATUS
	}

	public void received(String stringReceived) throws Exception {

		if (stringReceived == null || stringReceived.length() == 0) {
			return;
		}

		assert stringReceived.length() > 0;

		int index = 0;

		while (index < stringReceived.length()) {
			char c = stringReceived.charAt(index++);
			switch (currentParsingState) {
			case NOCONTEXT:
				switch (c) {
				case '<':
					currentParsingState = ParsingState.PARSINGSTATUS;
					buffer = new StringBuilder();
					break;
				case '\n':
				case '\r':
					if (buffer.length() > 0) {
						logger.warn("invalid state :" + buffer);
					}
					buffer = new StringBuilder();
					break;
				default:
					buffer.append(c);
					currentParsingState = ParsingState.READING;
				}
				break;
			case READING:
				switch (c) {
				case '<':
					if (buffer.toString().equalsIgnoreCase("ok")) { //$NON-NLS-1$
						commandFinished(buffer);
					} else if (buffer.length() > 0) {
						unknownCommandReceived(buffer);
					}

					currentParsingState = ParsingState.PARSINGSTATUS;
					buffer = new StringBuilder();
					break;
				case '\n':
					if (buffer.toString().equalsIgnoreCase("ok")) { //$NON-NLS-1$
						commandFinished(buffer);
					} else {
						if (buffer.length() > 0)
							unknownCommandReceived(buffer);
					}
					buffer = new StringBuilder();
					currentParsingState = ParsingState.NOCONTEXT;
					break;

				default:
					buffer.append(c);
					if (buffer.toString().equalsIgnoreCase("ok")) { //$NON-NLS-1$
						try {
							commandFinished(buffer);
						} catch (Exception ex) {
							logger.error(
									"error in command finished :" //$NON-NLS-1$
											+ ex.getMessage(), ex);
						}
						currentParsingState = ParsingState.NOCONTEXT;
						buffer = new StringBuilder();
					}

				}
				break;
			case PARSINGSTATUS:
				if (c == '>') {
					try {
						decomposeStatusBuffer(buffer);
					} catch (Exception ex) {
						logger.error(
								"error in parsing status :" + ex.getMessage(), //$NON-NLS-1$
								ex);
					}
					buffer = new StringBuilder();

					currentParsingState = ParsingState.NOCONTEXT;
				} else {
					buffer.append(c);
				}
				break;

			}

		}

	}

	// ////////////////////////////////////////////////////////////////
	// event associated to machine state

	private void decomposeStatusBuffer(StringBuilder sb) throws Exception {
		logger.debug("status given :" + sb); //$NON-NLS-1$
		GRBLStatus status = GRBLStatus.parse(sb.toString());
		listener.statusReceived(status);
	}

	private void commandFinished(StringBuilder sb) {
		logger.debug("command finished :" + sb); //$NON-NLS-1$
		currentState = GRBLState.READY;
		listener.commandAck();
	}

	private void unknownCommandReceived(StringBuilder sb) {

		if (sb.toString().startsWith("Grbl")) { //$NON-NLS-1$
			logger.info("Machine ready to for sending commands"); //$NON-NLS-1$
			currentState = GRBLState.READY;
			listener.resetted();
			return;
		}

		logger.debug("unknown received :" + sb); //$NON-NLS-1$
		listener.unknownReceived(sb.toString());

	}

	// ///////////////////////////////////////////////////////////////////////
	// methods are synchronized

	/**
	 * send command to the GRBL
	 * 
	 * @param command
	 * @throws Exception
	 */
	public synchronized void sendCommand(String command) throws Exception {

		// THIS IS NOT TRUE
		// CHECK THIS
		// if (currentState != GRBLState.READY)
		// throw new Exception("not ready to accept new command");

		currentState = GRBLState.WAITING_COMMAND_ACK;

		sendString.sendString(command);
	}

	public synchronized void sendStatusRequest() throws Exception {

		if (currentState != GRBLState.UNKNOWN) {
			sendString.sendString("?"); //$NON-NLS-1$
		}

	}

}
