package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GRBLPunchCompilerVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.testserialport.NoOpSerialPort;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;
import org.junit.Test;

public class TestMachineDefectsBehaviour {

	private static Logger logger = Logger.getLogger(TestMachineDefectsBehaviour.class);

	// GRBL STATES : Idle, Run, Hold, Jog, Alarm, Door, Check, Home, Sleep

	// @Test
	public void testMachineOpening() throws Exception {

		BasicConfigurator.configure();

		GRBLMachineControl.serialPortClass = NoOpSerialPort.class;

		GRBLMachineControl c = new GRBLMachineControl("DUMMYPORT", new GRBLPunchCompilerVisitor());
		c.setMachineControlListener(new MachineControlListener() {

			@Override
			public void statusChanged(MachineStatus status) {
				System.out.println("status changed :" + status);
			}

			@Override
			public void rawCommandSent(String commandSent) {

			}

			@Override
			public void rawCommandReceived(String commandReceived) {

			}

			@Override
			public void error(String message) {
				System.err.println(message);
			}

			@Override
			public void currentMachinePosition(String status, double mx, double my) {
				System.out.println("current machine state :" + status + " :" + mx + " " + my);
			}
		});
		try {
			long l = 0;
			while (l++ < 100) {
				Thread.sleep(1000);
				if (l > 5) {

					NoOpSerialPort p = (NoOpSerialPort) c.serialPort;
					p.currentStatus = "Alarm";
				}
				logger.info("***** sending command");
				c.sendCommand(new PunchCommand(10, 10));
				logger.info("*****... sending command");
				
			}

		} finally {
			c.close();
		}

	}

}
