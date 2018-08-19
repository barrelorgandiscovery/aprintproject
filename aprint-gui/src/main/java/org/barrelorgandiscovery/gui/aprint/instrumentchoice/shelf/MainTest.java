package org.barrelorgandiscovery.gui.aprint.instrumentchoice.shelf;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class MainTest extends JFrame {

	public MainTest() throws HeadlessException {
		super("Music Shelf");

		buildContentPane();

		setSize(800, 500);
		setResizable(true);
		setLocationRelativeTo(null);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void buildContentPane() {
		getContentPane().setLayout(new StackLayout());
		add(new GradientPanel(), StackLayout.BOTTOM);
		add(new CDShelf(), StackLayout.TOP);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainTest tester = new MainTest();
				tester.setVisible(true);
			}
		});
	}
}