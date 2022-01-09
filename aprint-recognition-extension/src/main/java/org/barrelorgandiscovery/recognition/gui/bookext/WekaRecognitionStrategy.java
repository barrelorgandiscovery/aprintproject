package org.barrelorgandiscovery.recognition.gui.bookext;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.images.books.tools.IFamilyImageSeekerTiledImage;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.ShapeRoi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;
import weka.core.Instances;

public class WekaRecognitionStrategy implements IRecognitionStrategy {

	private static Logger logger = Logger.getLogger(WekaRecognitionStrategy.class);

	WekaSegmentation ws = null;

	private static int BOOK_CLASS = 0;
	private static int HOLES_CLASS = 1;

	private Scale scale;

	public WekaRecognitionStrategy(Scale scale) {
		assert scale != null;
		this.scale = scale;
	}

	public void reset() {
		ws = new WekaSegmentation();
	}

	public void train(List<Hole> bookHoles, List<Hole> holesHoles, IFamilyImageSeekerTiledImage familyTiledImage) throws Exception {

		Instances instances = null;

		assert bookHoles != null;
		assert holesHoles != null;

		for (Hole h : bookHoles) {
			instances = constructTrainingSet(instances, h, BOOK_CLASS, familyTiledImage);
		}

		for (Hole h : holesHoles) {
			instances = constructTrainingSet(instances, h, HOLES_CLASS, familyTiledImage);
		}

		if (instances == null) {
			logger.info("no training set, no recognition launched"); //$NON-NLS-1$
			return;
		}

		reset();

		updateSegmentationParameters(ws);
		ws.setLoadedTrainingData(instances);

		ws.doClassBalance();
		// ws.selectAttributes();
		ws.trainClassifier();
	}

	@Override
	public BufferedImage apply(BufferedImage image) {

		ImagePlus ip = new ImagePlus();
		ip.setImage(image);
		
		Prefs.setThreads(1);
		ImagePlus result = ws.applyClassifier(ip);

		ImageProcessor processor = result.getProcessor();
		processor.multiply(250);

		ByteProcessor bp = processor.convertToByteProcessor(true);

		ImagePlus binary = new ImagePlus("", bp); //$NON-NLS-1$

		return binary.getBufferedImage();
	}

	private void updateSegmentationParameters(WekaSegmentation ws) {

		// LinearRegression lr = new LinearRegression();
//		Logistic l = new Logistic();
//		l.setUseConjugateGradientDescent(true);
//		
//
//		MultiClassClassifier mcc = new MultiClassClassifier();
//		mcc.setClassifier(l);
//		mcc.setLogLossDecoding(true);
//		mcc.setNumDecimalPlaces(6);
//		mcc.setUsePairwiseCoupling(true);
//		
//		ws.setClassifier(mcc);
//		

		// ws.updateClassifier(200, 6, 10);

		boolean[] enabledFeatures = ws.getEnabledFeatures();
//		for (int i = 0 ; i < enabledFeatures.length ; i ++) {
//			enabledFeatures[i] = true;
//		}
		/*
		 * enabledFeatures[FeatureStack.DERIVATIVES] = false;
		 * 
		 * enabledFeatures[FeatureStack.GABOR] = true;
		 * enabledFeatures[FeatureStack.LIPSCHITZ] = true; //
		 * enabledFeatures[FeatureStack.HESSIAN] = true;
		 * enabledFeatures[FeatureStack.NEIGHBORS] = true;
		 * enabledFeatures[FeatureStack.GAUSSIAN] = true;
		 * 
		 * enabledFeatures[FeatureStack.VARIANCE] = true;
		 * enabledFeatures[FeatureStack.STRUCTURE] = true;
		 * 
		 * ws.setEnabledFeatures(enabledFeatures);
		 */
	}

	private Instances constructTrainingSet(Instances instances, Hole hole, int classNo, IFamilyImageSeekerTiledImage familyTiledImage) throws Exception {

	
		int height = familyTiledImage.getHeight();

		double mm = scale.timeToMM(hole.getTimestamp());

		double startPercent = (scale.getFirstTrackAxis() + hole.getTrack() * scale.getIntertrackHeight()
				- scale.getTrackWidth() / 2) / scale.getWidth();
		double endPercent = (scale.getFirstTrackAxis() + hole.getTrack() * scale.getIntertrackHeight()
				+ scale.getTrackWidth() / 2) / scale.getWidth();

		// fix acquisition when the scale is revert
		if (scale.isPreferredViewedInversed()) {
			startPercent = 1.0 - startPercent;
			endPercent = 1.0 - endPercent;
		}

		if (startPercent > endPercent) {
			double tmp = startPercent;
			startPercent = endPercent;
			endPercent = tmp;
		}

		int startTrackPixel = (int) (startPercent * height);
		int endTrackPixel = (int) (endPercent * height);

		int startPixel = (int) (mm / scale.getWidth() * height);
		int endPixel = (int) (scale.timeToMM(hole.getTimestamp() + hole.getTimeLength()) / scale.getWidth() * height);

		return addPixelsToInstances(instances, classNo, familyTiledImage, startTrackPixel, endTrackPixel, startPixel,
				endPixel);
	}

	/**
	 * add elements to training set from a pixel region
	 * 
	 * @param instances
	 * @param classNo
	 * @param familyTiledImage
	 * @param rectangle        zone in pixels
	 * @return
	 * @throws Exception
	 */
	private Instances addPixelsToInstances(Instances instances, int classNo,
			IFamilyImageSeekerTiledImage familyTiledImage, Rectangle2D rectangle) throws Exception {

		int x = (int) rectangle.getX();
		int y = (int) rectangle.getY();
		int width = (int) rectangle.getWidth();
		int height = (int) rectangle.getHeight();

		return addPixelsToInstances(instances, classNo, familyTiledImage, y, y + height, x, x + width);
	}

	/**
	 * add pixels into training set, using start / stop coordinates
	 * 
	 * @param instances
	 * @param classNo
	 * @param familyTiledImage
	 * @param startTrackPixel
	 * @param endTrackPixel
	 * @param startPixel
	 * @param endPixel
	 * @return
	 * @throws Exception
	 */
	private Instances addPixelsToInstances(Instances instances, int classNo,
			IFamilyImageSeekerTiledImage familyTiledImage, int startTrackPixel, int endTrackPixel, int startPixel,
			int endPixel) throws Exception {
		int imageTileWidth = familyTiledImage.getTileWidth();

		int startImageIndex = startPixel / imageTileWidth;
		int endImageIndex = endPixel / imageTileWidth;
		assert startImageIndex <= endImageIndex;

		Instances trainingInstances = instances;
		for (int i = startImageIndex; i <= endImageIndex; i++) {
			logger.debug("loading image :" + i); //$NON-NLS-1$
			BufferedImage bi = familyTiledImage.loadImage(startImageIndex);

			int offsetStart = startPixel - (i * imageTileWidth);
			int offsetEnd = endPixel - (i * imageTileWidth);

			Rectangle r = new Rectangle(offsetStart, startTrackPixel, offsetEnd - offsetStart,
					endTrackPixel - startTrackPixel);

			ImagePlus ip = new ImagePlus();
			ip.setImage(bi);

			WekaSegmentation ws = new WekaSegmentation(ip);
			updateSegmentationParameters(ws);

			ws.addExample(classNo, new ShapeRoi(r), 1);

			Instances t = ws.createTrainingInstances();
			if (t != null) {
				if (trainingInstances == null) {
					trainingInstances = t;
				} else {
					ws.mergeDataInPlace(trainingInstances, t);
				}
			}
		}
		return trainingInstances;
	}

}
