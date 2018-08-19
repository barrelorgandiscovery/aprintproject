package org.barrelorgandiscovery.recognition.gui.books.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.barrelorgandiscovery.scale.Scale;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;

/**
 * helper for large image recognition
 * 
 * @author pfreydiere
 *
 */
public class ImageBookProject {

	private Scale instrumentScale;
	private TiledImage ti;

	public ImageBookProject(TiledImage ti, Scale instrumentScale) throws Exception {
		assert ti != null;
		this.ti = ti;
		this.instrumentScale = instrumentScale;
	}

	public void recognize(int imageno) throws Exception {

		ImagePlus i = new ImagePlus(ti.constructImagePath(imageno, null).getAbsolutePath());

		WekaSegmentation ws = new WekaSegmentation(i);
		ws.loadClassifier(
				"C:\\projets\\APrint\\contributions\\Jean Pierre Rosset\\numerisation\\classifier_trou_carton_23122016.model");

		ImagePlus r = ws.applyClassifier(i);

		ImageProcessor processor = r.getProcessor();
		processor.setAutoThreshold(ij.process.ImageProcessor.ISODATA2, ij.process.ImageProcessor.BLACK_AND_WHITE_LUT);

		ByteProcessor bp = processor.convertToByteProcessor();
		bp.invert();

		ImagePlus binary = new ImagePlus("", bp);
		ij.IJ.save(binary, ti.constructImagePath(imageno, "rec").getAbsolutePath());

		// remove noise
		for (int j = 0; j < 20; j++) {
			bp.dilate();
		}
		for (int j = 0; j < 20; j++) {
			bp.erode();
		}

		ij.IJ.save(binary, ti.constructImagePath(imageno, "rec_background").getAbsolutePath());

	}

}
