package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GRBLPunchCompilerVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.testserialport.DebugSerialGRBL11;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.testserialport.NoOpSerialPort;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
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
			public void rawElementSent(String commandSent) {

			}

			@Override
			public void rawElementReceived(String commandReceived) {

			}

			@Override
			public void error(String message) {
				System.err.println(message);
			}

			@Override
			public void currentMachinePosition(String status, double mx, double my) {
				System.out.println("current machine state :" + status + " :" + mx + " " + my);
			}

			@Override
			public void informationReceived(String commands) {
				System.out.println(commands);
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

	// @Test
	public void testGRBLLazerCommunication() throws Exception {

		BufferedImage bufferedImage = new BufferedImage(1000, 200, BufferedImage.TYPE_4BYTE_ABGR);

		JFrame jFrame = new JFrame();
		JLabel label = new JLabel();
		jFrame.getContentPane().setLayout(new BorderLayout());
		jFrame.getContentPane().add(label, BorderLayout.CENTER);
		jFrame.setSize(1000, 200);

		jFrame.setVisible(true);

		BasicConfigurator.configure(new LF5Appender());

		GRBLLazerMachineParameters mp = new GRBLLazerMachineParameters();
		mp.comPort = "COM3";

		GRBLMachineControl.serialPortClass = DebugSerialGRBL11.class;

		AbstractMachine machine = mp.createAssociatedMachineInstance();
		MachineControl control = machine.open(mp);

		Thread.sleep(5000);

		

		control.setMachineControlListener(new MachineControlListener() {

			@Override
			public void statusChanged(MachineStatus status) {
				System.out.println("status changed :" + status);
			}

			@Override
			public void rawElementSent(String commandSent) {

			}

			@Override
			public void rawElementReceived(String commandReceived) {

			}

			@Override
			public void error(String message) {
				System.err.println(message);
			}

			@Override
			public void currentMachinePosition(String status, double mx, double my) {
				System.out.println("current machine state :" + status + " :" + mx + " " + my);
				

				SwingUtilities.invokeLater(() -> {

					Graphics graphics = bufferedImage.getGraphics();
					try {
						graphics.drawRect((int) mx, (int) my, 1, 1);

					} finally {
						graphics.dispose();
					}
					label.setIcon(new ImageIcon(bufferedImage));
					jFrame.repaint();
					
				});
			}

			@Override
			public void informationReceived(String commands) {
				System.out.println(commands);
			}

		});

		for (int i = 0; i < 100; i++) {
			DisplacementCommand c = new DisplacementCommand((i % 2) * 10, i * 10);
			logger.debug("sending command :" + c);
			control.sendCommand(c);
		}

		control.flushCommands();

	}

}
