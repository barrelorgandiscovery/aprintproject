package org.barrelorgandiscovery.extensionsng.perfo.ng;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * test to evaluate the possibility to directly program microcontroller instead
 * of including avrdude
 * 
 * @author pfreydiere
 *
 */
public class TestAVRUpload {

	private SerialPort serialPort;

	@Before
	public void setup() {

	}

	public char toDigit(int i) {
		return (char) ('0' + i);
	}

	public String hexByteDump(int i) {
		return "0x" + toDigit((i >> 4) & 0x0F) + toDigit(i & 0x0F);
	}

	public String hexByteDump(byte[] bytes) {
		String s = "";
		for (int i = 0; i < bytes.length; i++) {
			s += " " + hexByteDump(bytes[i]);
		}
		return s;
	}

	final BlockingQueue<Integer> received = new LinkedBlockingQueue<>();

	private byte[] sendCmd(byte[] cmd) throws Exception {

		received.clear();

		byte[] buffer = new byte[6];

		buffer[0] = Cmnd_STK_UNIVERSAL;

		buffer[1] = cmd[0];
		buffer[2] = cmd[1];
		buffer[3] = cmd[2];
		buffer[4] = cmd[3];
		buffer[5] = Sync_CRC_EOP;

		send(buffer);

		byte[] responseBuffer = new byte[4];

		responseBuffer[0] = cmd[1];
		responseBuffer[1] = cmd[2];
		responseBuffer[2] = cmd[3];

		int response = readByte();
		responseBuffer[3] = (byte) response;

		response = readByte();
		if (response != Resp_STK_OK)
			throw new Exception("returned " + response);
		return responseBuffer;
	}

	private byte[] getparam(int param, int retry) throws Exception {
		if (retry < 0) {
			throw new Exception("max retry reach for get param");
		}
		byte[] r = new byte[3];
		r[0] = Cmnd_STK_GET_PARAMETER;
		r[1] = (byte) param;
		r[2] = Sync_CRC_EOP;
		received.clear();
		send(r);

		int response = readByte();
		if (response == Resp_STK_NOSYNC) {
			getSync();
			return getparam(param, retry - 1);
		}
		assert response == Resp_STK_INSYNC;

		byte[] result = new byte[1];
		response = readByte();
		result[0] = (byte) response;

		response = readByte();
		if (response != Resp_STK_OK) {
			throw new Exception("failed with return " + response);
		}
		return result;
	}

	private Integer readByte() throws Exception {
		Integer r = received.poll(1, TimeUnit.SECONDS);
		if (r == null) {
			throw new Exception("no result");
		}
		return r;
	}

	@Test
	public void testSerial() throws Exception {

		class PortReader implements SerialPortEventListener {
			@Override
			public void serialEvent(SerialPortEvent event) {
				System.out.println("serial received");
				if (event.isRXCHAR() && event.getEventValue() > 0) {
					try {
						String receivedData;

						receivedData = serialPort.readString(event.getEventValue());

						System.out.println("received data from serial :" + receivedData + "\n");

						for (int i = 0; i < receivedData.length(); i++) {
							int c = (int) receivedData.charAt(i);
							received.add(c);
							System.out.println(" " + i + " :" + c + " " + hexByteDump(c));
						}

					} catch (Exception ex) {
						System.err.println("Error in receiving string from COM-port: " + ex);
						ex.printStackTrace();
					}
				}
			}

		}

		String portName = "COM4";
		serialPort = new SerialPort(portName);

		System.out.println("opening " + portName);

		serialPort.openPort();

		serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);

		serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR | SerialPort.MASK_RXFLAG);

		serialPort.setDTR(false);
		serialPort.setRTS(false);
		Thread.sleep(50);
		serialPort.setDTR(true);
		serialPort.setRTS(true);
		Thread.sleep(50);

		// send multiple sync, for noise
		getSync();
		getSync();
		getSync();

		// read response
		int resp = readByte();
		assert resp == Resp_STK_INSYNC;
		resp = readByte();
		assert resp == Resp_STK_OK;

		byte[] resultmajor = getparam(Parm_STK_SW_MAJOR, 3);
		System.out.println("HW version :" + hexByteDump(resultmajor));
		byte[] resultminor = getparam(Parm_STK_SW_MINOR, 3);
		System.out.println("HW version :" + hexByteDump(resultminor));

		Thread.sleep(10_000);

		System.out.println("end");

	}

	public static final String STK_SIGN_ON_MESSAGE = "AVR STK"; // Sign on string for Cmnd_STK_GET_SIGN_ON

	// *****************[ STK Response constants ]***************************

	public static final int Resp_STK_OK = 0x10; // ' '
	public static final int Resp_STK_FAILED = 0x11; // ' '
	public static final int Resp_STK_UNKNOWN = 0x12; // ' '
	public static final int Resp_STK_NODEVICE = 0x13; // ' '
	public static final int Resp_STK_INSYNC = 0x14; // ' '
	public static final int Resp_STK_NOSYNC = 0x15; // ' '

	public static final int Resp_ADC_CHANNEL_ERROR = 0x16; // ' '
	public static final int Resp_ADC_MEASURE_OK = 0x17; // ' '
	public static final int Resp_PWM_CHANNEL_ERROR = 0x18; // ' '
	public static final int Resp_PWM_ADJUST_OK = 0x19; // ' '

	// *****************[ STK Special constants ]***************************

	public static final int Sync_CRC_EOP = 0x20; // 'SPACE'

	// *****************[ STK Command constants ]***************************

	public static final int Cmnd_STK_GET_SYNC = 0x30; // ' '
	public static final int Cmnd_STK_GET_SIGN_ON = 0x31;// ' '

	public static final int Cmnd_STK_SET_PARAMETER = 0x40; // ' '
	public static final int Cmnd_STK_GET_PARAMETER = 0x41; // ' '
	public static final int Cmnd_STK_SET_DEVICE = 0x42; // ' '
	public static final int Cmnd_STK_SET_DEVICE_EXT = 0x45; // ' '

	public static final int Cmnd_STK_ENTER_PROGMODE = 0x50; // ' '
	public static final int Cmnd_STK_LEAVE_PROGMODE = 0x51; // ' '
	public static final int Cmnd_STK_CHIP_ERASE = 0x52; // ' '
	public static final int Cmnd_STK_CHECK_AUTOINC = 0x53; // ' '
	public static final int Cmnd_STK_LOAD_ADDRESS = 0x55; // ' '
	public static final int Cmnd_STK_UNIVERSAL = 0x56;// ' '
	public static final int Cmnd_STK_UNIVERSAL_MULTI = 0x57; // ' '

	public static final int Cmnd_STK_PROG_FLASH = 0x60; // ' '
	public static final int Cmnd_STK_PROG_DATA = 0x61; // ' '
	public static final int Cmnd_STK_PROG_FUSE = 0x62; // ' '
	public static final int Cmnd_STK_PROG_LOCK = 0x63; // ' '
	public static final int Cmnd_STK_PROG_PAGE = 0x64; // ' '
	public static final int Cmnd_STK_PROG_FUSE_EXT = 0x65; // ' '

	public static final int Cmnd_STK_READ_FLASH = 0x70;// ' '
	public static final int Cmnd_STK_READ_DATA = 0x71;// ' '
	public static final int Cmnd_STK_READ_FUSE = 0x72; // ' '
	public static final int Cmnd_STK_READ_LOCK = 0x73; // ' '
	public static final int Cmnd_STK_READ_PAGE = 0x74; // ' '
	public static final int Cmnd_STK_READ_SIGN = 0x75; // ' '
	public static final int Cmnd_STK_READ_OSCCAL = 0x76; // ' '
	public static final int Cmnd_STK_READ_FUSE_EXT = 0x77; // ' '
	public static final int Cmnd_STK_READ_OSCCAL_EXT = 0x78; // ' '

	// *****************[ STK Parameter constants ]***************************

	public static final int Parm_STK_HW_VER = 0x80; // ' ' - R
	public static final int Parm_STK_SW_MAJOR = 0x81; // ' ' - R
	public static final int Parm_STK_SW_MINOR = 0x82; // ' ' - R
	public static final int Parm_STK_LEDS = 0x83; // ' ' - R/W
	public static final int Parm_STK_VTARGET = 0x84; // ' ' - R/W
	public static final int Parm_STK_VADJUST = 0x85; // ' ' - R/W
	public static final int Parm_STK_OSC_PSCALE = 0x86; // ' ' - R/W
	public static final int Parm_STK_OSC_CMATCH = 0x87; // ' ' - R/W
	public static final int Parm_STK_RESET_DURATION = 0x88; // ' ' - R/W
	public static final int Parm_STK_SCK_DURATION = 0x89; // ' ' - R/W

	public static final int Parm_STK_BUFSIZEL = 0x90; // ' ' - R/W, Range {0..255}
	public static final int Parm_STK_BUFSIZEH = 0x91; // ' ' - R/W, Range {0..255}
	public static final int Parm_STK_DEVICE = 0x92; // ' ' - R/W, Range {0..255}
	public static final int Parm_STK_PROGMODE = 0x93; // ' ' - 'P' or 'S'
	public static final int Parm_STK_PARAMODE = 0x94; // ' ' - TRUE or FALSE
	public static final int Parm_STK_POLLING = 0x95; // ' ' - TRUE or FALSE
	public static final int Parm_STK_SELFTIMED = 0x96; // ' ' - TRUE or FALSE
	public static final int Param_STK500_TOPCARD_DETECT = 0x98; // ' ' - Detect top-card attached

	// *****************[ STK status bit definitions ]***************************

	public static final int Stat_STK_INSYNC = 0x01; // INSYNC status bit, '1' - INSYNC
	public static final int Stat_STK_PROGMODE = 0x02; // Programming mode, '1' - PROGMODE
	public static final int Stat_STK_STANDALONE = 0x04; // Standalone mode, '1' - SM mode
	public static final int Stat_STK_RESET = 0x08; // RESET button, '1' - Pushed
	public static final int Stat_STK_PROGRAM = 0x10; // Program button, ' 1' - Pushed
	public static final int Stat_STK_LEDG = 0x20; // Green LED status, '1' - Lit
	public static final int Stat_STK_LEDR = 0x40; // Red LED status, '1' - Lit
	public static final int Stat_STK_LEDBLINK = 0x80;// LED blink ON/OFF, '1' - Blink

	// *****************************[ End Of COMMAND.H ]**************************

	private static byte tobyte(int i) {
		byte b = (byte) (i & 0xFF);
		return b;
	}

	private void sync_on() throws Exception {

		byte[] cmd = new byte[] { tobyte(Cmnd_STK_GET_SIGN_ON), tobyte(Sync_CRC_EOP) };

	}

	private void getVersion() throws Exception {
		byte[] cmd = new byte[] { tobyte(Cmnd_STK_READ_SIGN), tobyte(Sync_CRC_EOP) };
		send(cmd);

	}

	private boolean send(byte[] cmd) throws SerialPortException {
		System.out.println("send " + hexByteDump(cmd));
		return serialPort.writeBytes(cmd);
	}

	private void getSync() throws Exception {
		byte[] cmd = new byte[] { tobyte(Cmnd_STK_GET_SYNC), tobyte(Sync_CRC_EOP) };
		send(cmd);
		Thread.sleep(10);
	}

	@Test
	public void testConvert() {
		System.out.println("" + tobyte(0xFE));
	}
}
