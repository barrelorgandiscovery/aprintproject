package org.barrelorgandiscovery.gui.ascale.constraints;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.scale.ConstraintList;


public class TestConstraintsPanel extends JFrame {

	private static Logger logger = Logger.getLogger(TestConstraintsPanel.class);

	public TestConstraintsPanel() {
		setLayout(new BorderLayout());
		ConstraintPanel constraintPanel = new ConstraintPanel();
		constraintPanel
				.setConstraintListListener(new ConstraintListChangeListener() {
					public void constraintListChanged(
							ConstraintList newConstraintList) {
						logger.debug("constraint changed " + newConstraintList); //$NON-NLS-1$
					}
				});

		getContentPane().add(constraintPanel, BorderLayout.CENTER);
		setSize(300, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		BasicConfigurator.configure(new LF5Appender());

		new TestConstraintsPanel().setVisible(true);
	}

}
