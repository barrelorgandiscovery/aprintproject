package org.barrelorgandiscovery.ui.animation;

/**
 * About this Code
 *
 * The original code is from Romain Guy's example "A Music Shelf in Java2D".
 * It can be found here:
 *
 *   http://www.curious-creature.org/2005/07/09/a-music-shelf-in-java2d/
 *
 * Updated Code
 * This code has been updated by Kevin Long (codebeach.com) to make it more
 * generic and more component like.
 *
 * History:
 *
 * 2/17/2008
 * ---------
 * - Removed hard coded strings for labels and images
 * - Removed requirement for images to be included in the jar
 * - Removed CD case drawing
 * - Support for non-square images
 * - Support for loading images from thumbnails
 * - External methods to set and get currently selected item
 * - Added support for ListSelectionListener
 */

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MainTest extends JFrame {
	private JLabel currentItem;
	private ImageFlow imageFlow = null;

	public MainTest() throws HeadlessException {
		super("Image Flow");

		buildContentPane();

		setSize(640, 360);
		setResizable(true);
		setLocationRelativeTo(null);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void buildContentPane() {

		JPanel imageFlowPanel = new JPanel(new StackLayout());
		imageFlowPanel.add(new GradientPanel(), StackLayout.BOTTOM);

		ArrayList<ImageFlowItem> items = new ArrayList<ImageFlowItem>();
		try {
			items.add(new ImageFlowItem(new File("gammes/2729Turlutain.jpg"),
					"Colosseum"));
			items.add(new ImageFlowItem(new File("gammes/43Jipe-1.jpg"),
					"Horse"));
			items.add(new ImageFlowItem(new File(
					"gammes/29_Robert_Hopp_Anches.jpg"), "London"));

			items.add(new ImageFlowItem(new File("gammes/2729Turlutain.jpg"),
					"Colosseum"));
			items.add(new ImageFlowItem(new File("gammes/43Jipe-1.jpg"),
					"Horse"));
			items.add(new ImageFlowItem(new File(
					"gammes/29_Robert_Hopp_Anches.jpg"), "London"));

			// items.add(new ImageFlowItem(new
			// File("c:/projects/components/images/tent.jpg"), "Tent"));
			// items.add(new ImageFlowItem(new
			// File("c:/projects/components/images/Tomatoes.jpg"), "Tomatoes"));
			// items.add(new ImageFlowItem(new
			// File("c:/projects/components/images/Vegetables.jpg"),
			// "Vegetables"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		try {
			imageFlow = new ImageFlow(items);

			// imageFlow.setSigma(0.3);
			// imageFlow.setSpacing(0.5);
			imageFlow.setAmount(5);
			// imageFlow.setAmount(10);
			imageFlowPanel.add(imageFlow, StackLayout.TOP);

			imageFlow.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					System.out.println("list selection item event :" + e);
					System.out.println(e.getFirstIndex());
				}
			});
		} catch (Exception e) {

		}

		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(imageFlowPanel, BorderLayout.CENTER);

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
