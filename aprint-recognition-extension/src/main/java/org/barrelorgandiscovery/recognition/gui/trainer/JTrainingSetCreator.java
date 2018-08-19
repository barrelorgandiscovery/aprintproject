package org.barrelorgandiscovery.recognition.gui.trainer;

import ij.IJ;
import ij.ImagePlus;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JImageDisplayLayer;

import trainableSegmentation.WekaSegmentation;

/**
 * panel for creating training sets
 * 
 * @author pfreydiere
 * 
 */
public class JTrainingSetCreator extends JPanel {

	public static void main(String[] args) throws Exception {

		JFrame f = new JFrame();
		f.setLayout(new BorderLayout());

		final JDisplay disp = new JDisplay();

		final JImageDisplayLayer jImageDisplayLayer = new JImageDisplayLayer();
		jImageDisplayLayer.setImageToDisplay(ImageIO.read(new File(
				"c:\\temp\\recognition\\modifiedImage_cropped.png")));

		disp.addLayer(jImageDisplayLayer);

		f.setSize(800, 800);

		f.getContentPane().add(disp, BorderLayout.CENTER);

		JButton jmc = new JButton("execute model");
		f.getContentPane().add(jmc, BorderLayout.NORTH);
		jmc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ImagePlus input = new ImagePlus();
					input.setImage(jImageDisplayLayer.getImageToDisplay());

					WekaSegmentation ws = new WekaSegmentation(input);
					ws.loadClassifier("C:\\temp\\recognition\\classifier.model");
					ImagePlus r = ws.applyClassifier(input);

					JImageDisplayLayer l = new JImageDisplayLayer();
					l.setImageToDisplay(r.getBufferedImage());
					disp.addLayer(l);
					disp.repaint();

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		f.setVisible(true);

	}

}
