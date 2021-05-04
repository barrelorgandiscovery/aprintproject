package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial;

public class SerialPortException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SerialPortException() {
		super();
	}

	public SerialPortException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SerialPortException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerialPortException(String message) {
		super(message);
	}

	public SerialPortException(Throwable cause) {
		super(cause);
	}

}
