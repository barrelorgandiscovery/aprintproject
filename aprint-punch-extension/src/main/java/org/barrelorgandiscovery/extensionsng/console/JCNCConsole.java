package org.barrelorgandiscovery.extensionsng.console;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineDirectControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLLazerMachineParameters;

/**
 * this panel show the grbl commands passed to machine
 * @author pfreydiere
 *
 */
public class JCNCConsole extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8200126114946678782L;

	private static Logger logger = Logger.getLogger(JCNCConsole.class);

	private MachineControl machine;

	private JScrollPane scrollPane;
	private JConsole console;

	private JTextField textField;

	private MachineControlListener listener = new MachineControlListener() {

		@Override
		public void statusChanged(MachineStatus status) {
		}

		@Override
		public void rawElementSent(String commandSent) {
		}

		@Override
		public void rawElementReceived(String commandReceived) {
		}
		
		@Override
		public void informationReceived(String infos) {
			SwingUtilities.invokeLater(() -> {
				if (infos != null  && !infos.startsWith("<")) {
					console.writeln(infos);
					console.repaint();
				}
			});
		}

		
		@Override
		public void error(String message) {
			SwingUtilities.invokeLater(() -> {
				console.write(message);
				console.repaint();
			});
		}

		@Override
		public void currentMachinePosition(String status, double mx, double my) {

		}
	};

	public JCNCConsole(MachineDirectControl machine) throws Exception {
		assert machine != null;
		this.machine = machine;

		if (!(machine instanceof MachineDirectControl)) {
			throw new Exception("machine must implements " + MachineDirectControl.class.getName());
		}
		machine.setMachineControlListener(listener);
		initComponents();
		console.setCursorPos(0, console.getRows() - 1);
		console.setCursorBlink(true);
		console.repaint();
	}

	protected void initComponents() throws Exception {
		setLayout(new BorderLayout());

		JConsole jConsole = new JConsole();
		this.console = jConsole;

		jConsole.setPreferredSize(new Dimension(800, 1000));

		scrollPane = new JScrollPane(jConsole);
		add(scrollPane, BorderLayout.CENTER);

		scrollPane.setAutoscrolls(true);
		
		
		scrollPane.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				console.setCursorPos(0, console.getRows() - 1);
			}
		});
		
		JTextField jTextField = new JTextField();
		this.textField = jTextField;

		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					String command = textField.getText();
					textField.setText("");
					try {
						((MachineDirectControl) machine).directCommand(command + "\n");
						console.writeln("\n >>" + command + "\n");
						console.repaint();
					} catch (Exception ex) {
						logger.error("error while sending command :" + ex.getMessage(), ex);
					}
				}
			}
		});

		add(jTextField, BorderLayout.SOUTH);
		scrollPane.getViewport().setViewPosition(new Point(0,100000));
	}

	// test method
	public static void main(String[] args) throws Exception {
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());

		GRBLLazerMachineParameters p = new GRBLLazerMachineParameters();
		p.setComPort("COM4");

		AbstractMachine machine = p.createAssociatedMachineInstance();
		MachineControl mc = machine.open(p);

		JCNCConsole jgrblConsole = new JCNCConsole((MachineDirectControl)mc);
		f.getContentPane().add(jgrblConsole, BorderLayout.CENTER);

		f.setSize(800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
