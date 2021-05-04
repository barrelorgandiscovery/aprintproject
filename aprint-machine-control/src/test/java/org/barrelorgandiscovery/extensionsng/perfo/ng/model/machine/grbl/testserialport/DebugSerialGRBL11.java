package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.testserialport;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.ISerialPort;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPort;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortEventListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException;

import jssc.SerialPortEvent;

/**
 * this class act as a middleware between machine and application to dump the
 * behaviour of communication
 * 
 * @author pfreydiere
 *
 */
public class DebugSerialGRBL11 implements ISerialPort {

	private Logger logger = Logger.getLogger(DebugSerialGRBL11.class);

	SerialPort inner;

	public DebugSerialGRBL11(String portName) {

		inner = new SerialPort(portName);

	}

	@Override
	public String readString(int n) throws SerialPortException {

		String readString = inner.readString(n);
		logger.debug("readString :" + readString);

		return readString;
	}

	@Override
	public void closePort() throws SerialPortException {
		logger.debug("close Port");
		inner.closePort();
	}

	@Override
	public void openPort() throws SerialPortException {

		logger.debug("openPort");
		inner.openPort();
	}

	@Override
	public void setParams(int baudrate, int databit, int stopbits, int parity) throws SerialPortException {

		inner.setParams(baudrate, databit, stopbits, parity);

	}

	@Override
	public void writeString(String s) throws SerialPortException {
		logger.debug("writeString " + s);
		System.out.println("Writing serial string :" + s + "\n");
		inner.writeString(s);
	}

	@Override
	public void addEventListener(SerialPortEventListener listener, int mask) throws SerialPortException {

		SerialPortEventListener added = new SerialPortEventListener() {
			@Override
			public void serialEvent(SerialPortEvent arg0) {
				logger.debug("event listener event :" + arg0);
				listener.serialEvent(arg0);
			}
		};

		inner.addEventListener(added, mask);

	}

}
