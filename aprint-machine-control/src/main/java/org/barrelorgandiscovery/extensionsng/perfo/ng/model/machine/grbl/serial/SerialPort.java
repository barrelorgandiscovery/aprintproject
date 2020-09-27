package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial;

public class SerialPort implements ISerialPort {

	jssc.SerialPort innerSerialPort;

	public SerialPort(String portName) {
		this.innerSerialPort = new jssc.SerialPort(portName);
	}

	/* (non-Javadoc)
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.ISerialPort#readString(int)
	 */
	@Override
	public String readString(int n)
			throws org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException {
		try {
			return innerSerialPort.readString(n);
		} catch (jssc.SerialPortException ex) {
			throw new org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException(
					ex.getMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.ISerialPort#closePort()
	 */
	@Override
	public void closePort() throws SerialPortException {
		try {
			// this function don't work and don't clear the event listener
			// the issue is that the event is called multiple times, 

			try {
				innerSerialPort.removeEventListener();
			} catch(Throwable t) {};
			
			innerSerialPort.closePort();
			
			
			
		} catch (jssc.SerialPortException ex) {
			throw new org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException(
					ex.getMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.ISerialPort#openPort()
	 */
	@Override
	public void openPort() throws SerialPortException {
		try {
			innerSerialPort.openPort();
		} catch (jssc.SerialPortException ex) {
			throw new org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException(
					ex.getMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.ISerialPort#setParams(int, int, int, int)
	 */
	@Override
	public void setParams(int baudrate, int databit, int stopbits, int parity) throws SerialPortException {
		try {
			innerSerialPort.setParams(baudrate, databit, stopbits, parity);
		} catch (jssc.SerialPortException ex) {
			throw new org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException(
					ex.getMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.ISerialPort#writeString(java.lang.String)
	 */
	@Override
	public void writeString(String s) throws SerialPortException {
		try {
			innerSerialPort.writeString(s);
		} catch (jssc.SerialPortException ex) {
			throw new org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException(
					ex.getMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.ISerialPort#addEventListener(org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortEventListener, int)
	 */
	@Override
	public void addEventListener(SerialPortEventListener listener, int mask) throws SerialPortException {
		try {
			innerSerialPort.addEventListener(listener, mask);
		} catch (jssc.SerialPortException ex) {
			throw new org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException(
					ex.getMessage(), ex);
		}
	}
}
