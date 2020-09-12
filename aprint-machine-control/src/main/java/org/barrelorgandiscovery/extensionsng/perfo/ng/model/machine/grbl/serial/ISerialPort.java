package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial;

/**
 * interface used for mocking the serialport
 * 
 * @author pfreydiere
 *
 */
public interface ISerialPort {

	String readString(int n)
			throws org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException;

	void closePort() throws SerialPortException;

	void openPort() throws SerialPortException;

	void setParams(int baudrate, int databit, int stopbits, int parity) throws SerialPortException;

	void writeString(String s) throws SerialPortException;

	void addEventListener(SerialPortEventListener listener, int mask) throws SerialPortException;

}