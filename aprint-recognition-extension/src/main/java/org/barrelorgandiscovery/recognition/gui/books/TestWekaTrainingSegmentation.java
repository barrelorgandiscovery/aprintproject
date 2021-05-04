package org.barrelorgandiscovery.recognition.gui.books;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ShapeRoi;
import trainableSegmentation.WekaSegmentation;
import weka.core.Instances;

/**
 * test method to train datas
 * 
 * @author pfreydiere
 *
 */
public class TestWekaTrainingSegmentation {

	private static void show(Image image) {
		JFrame f = new JFrame();
		f.setSize(600, 400);
		JLabel l = new JLabel();
		l.setIcon(new ImageIcon(image));
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(l, BorderLayout.CENTER);
		f.setVisible(true);
	}

	public static void main(String[] args) throws Exception {

		File inputImage = new File(
				"C:\\projets\\APrint\\contributions\\plf\\2020-10_Essais saisie video\\test1411\\image_0002.jpg");
		ImagePlus i = new ImagePlus(inputImage.getAbsolutePath());

		BufferedImage labeled = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);

		Rectangle rect = new Rectangle(176, 377, 29, 14);

		WekaSegmentation ws = new WekaSegmentation(i);

		ws.addExample(0, new ShapeRoi(rect), 1);
		Rectangle r2 = new Rectangle(176, 377, 29, 200);
		r2.translate(0, 30);
		ws.addExample(1, new ShapeRoi(r2), 1);

		ws.trainClassifier();

		Instances trainingDatas = ws.getTraceTrainingData();
		// System.out.println(trainingDatas);
		
		WekaSegmentation ws2 = new WekaSegmentation();
		ws2.setLoadedTrainingData(trainingDatas);
		ws2.trainClassifier();
		
		ImagePlus result = ws2.applyClassifier(i);

		IJ.save(result, "c:\\temp\\resultfirstclass.png");

		show(result.getImage());

	}

}
