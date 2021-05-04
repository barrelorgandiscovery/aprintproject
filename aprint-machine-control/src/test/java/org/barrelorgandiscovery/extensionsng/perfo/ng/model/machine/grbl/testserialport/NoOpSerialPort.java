package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.testserialport;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.ISerialPort;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortEventListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.serial.SerialPortException;

import jssc.SerialPortEvent;

public class NoOpSerialPort implements ISerialPort {

	private static Logger logger = Logger.getLogger(NoOpSerialPort.class);

	private ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

	private ScheduledExecutorService portHandling = Executors.newSingleThreadScheduledExecutor();

	public String currentStatus = "Idle";

	public static class C {
		String request;
		String response;
	}

	private ConcurrentLinkedQueue<C> commandQueues = new ConcurrentLinkedQueue<>();

	public NoOpSerialPort(String portName) {

		logger.info("create NoOpSerialPort " + portName);
	}

	protected void enqueue(String request) {
		C c = new C();
		c.request = request;

		String response = handleRequest(request);
		c.response = response;
		commandQueues.add(c);

	}

	protected String handleRequest(String request) {

		if ("?".equals(request)) {
			return "<" + currentStatus + ",MPos:10,23,45,WPos:10,20,60>";
		} else

		{
			if ("Alarm".equals(currentStatus)) {
				return "Error";
			} else {

				if ("M100".equals(request)) {
					return "ok";
				}
				return "ok";

			}
		}

	}

	@Override
	public String readString(int n) throws SerialPortException {

		if (!commandQueues.isEmpty()) {
			C nextCommand = commandQueues.poll();
			logger.info("sending " + nextCommand.response);
			return nextCommand.response + "\n";
		}

		return null;
	}

	@Override
	public void closePort() throws SerialPortException {
		logger.info("close port");
	}

	@Override
	public void openPort() throws SerialPortException {
		logger.info("Open port");
		// sending hello string
		C c = new C();
		c.response = "Grbl\n";
		commandQueues.add(c);

		portHandling.scheduleWithFixedDelay(() -> {
			try {
				Thread.sleep(20);
				if (!commandQueues.isEmpty()) {
					// signal there is a string to grab
					listener.serialEvent(new SerialPortEvent("", SerialPortEvent.RXCHAR, 3));
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}, 1, 1, TimeUnit.SECONDS);

	}

	@Override
	public void setParams(int baudrate, int databit, int stopbits, int parity) throws SerialPortException {

	}

	String currentResponse;

	@Override
	public void writeString(String s) throws SerialPortException {
		logger.info("write string :" + s);
		// send call back
		System.out.println(s);

		enqueue(s);

	}

	private SerialPortEventListener listener;

	@Override
	public void addEventListener(SerialPortEventListener listener, int mask) throws SerialPortException {
		this.listener = listener;

	}

}
