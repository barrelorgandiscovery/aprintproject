package org.barrelorgandiscovery.recognition.gui.bookext;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.images.books.tools.IFamilyImageSeekerTiledImage;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.ShapeRoi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;
import weka.core.Instances;

/**
 * recognition algorithm using ML algorithms
 * 
 * @author pfreydiere
 *
 */
public class WekaRecognitionStrategy implements IRecognitionStrategy {

	private static Logger logger = Logger.getLogger(WekaRecognitionStrategy.class);

	private static int BOOK_CLASS = 0;
	private static int HOLES_CLASS = 1;

	private Scale scale;

	public WekaRecognitionStrategy(Scale scale) {
		assert scale != null;
		this.scale = scale;
	}

	public void reset() {
		currentlyUsed = null;

	}

	private Map<Integer, List<ClassROI>> perTileShapes = new HashMap<>();

	private static class ClassROI {

		public final int classe;
		public final ShapeRoi shape;

		public ClassROI(int classe, ShapeRoi shape) {
			this.classe = classe;
			this.shape = shape;
		}
	}

	private static class ModelEvaluation {
		public ModelEvaluation(int n0, int n1, WekaSegmentation ws) {
			this.nb0 = n0;
			this.nb1 = n1;
			this.ws = ws;
		}

		final int nb1;
		final int nb0;
		final WekaSegmentation ws;
	}

	WekaSegmentation currentlyUsed = null;

	/**
	 * train the model
	 * 
	 * @param bookHoles
	 * @param freeBookRects
	 * @param holesHoles
	 * @param freeHolesRects
	 * @param familyTiledImage
	 * @throws Exception
	 */
	public void train(List<Hole> bookHoles, List<Rectangle2D.Double> freeBookRects, List<Hole> holesHoles,
			List<Rectangle2D.Double> freeHolesRects, IFamilyImageSeekerTiledImage familyTiledImage) throws Exception {

		perTileShapes.clear();

		Instances instances = null;

		if (bookHoles != null) {
			for (Hole h : bookHoles) {
				constructTrainingSet(h, BOOK_CLASS, familyTiledImage);
			}
		}

		if (holesHoles != null) {
			for (Hole h : holesHoles) {
				constructTrainingSet(h, HOLES_CLASS, familyTiledImage);
			}
		}

		if (freeBookRects != null) {
			logger.debug("add book zones");
			for (Rectangle2D.Double r : freeBookRects) {
				constructTrainingSet(instances, BOOK_CLASS, familyTiledImage, r);
			}
		}

		if (freeHolesRects != null) {
			logger.debug("add holes zones");
			for (Rectangle2D.Double r : freeHolesRects) {
				constructTrainingSet(instances, HOLES_CLASS, familyTiledImage, r);
			}
		}

		// create a hole image with all the tiles, horizontally

		if (perTileShapes.size() == 0) {
			throw new Exception("no training set");
		}
		Integer it = perTileShapes.keySet().iterator().next();
		BufferedImage bufferedImage = familyTiledImage.loadImage(it);
		int tileWidth = bufferedImage.getWidth();
		BufferedImage whole = new BufferedImage(tileWidth * perTileShapes.keySet().size(), bufferedImage.getHeight(),
				bufferedImage.getType());

		int cpt = 0;
		Graphics2D g2d = whole.createGraphics();
		try {
			for (Entry<Integer, List<ClassROI>> e : perTileShapes.entrySet()) {
				g2d.drawImage(familyTiledImage.loadImage(e.getKey()), tileWidth * cpt++, 0, null);
			}

		} finally {
			g2d.dispose();
		}

		ImagePlus trainingImage = new ImagePlus();
		trainingImage.setImage(whole);
//		
//		JFrame t = new JFrame();
//		t.getContentPane()
//		.setLayout(new BorderLayout());
//		t.getContentPane()
//		.add(new JLabel(new ImageIcon(whole)), BorderLayout.CENTER);
//		t.pack();
//		t.setVisible(true);

		WekaSegmentation ws = new WekaSegmentation(trainingImage);

		cpt = 0;
		for (Entry<Integer, List<ClassROI>> e : perTileShapes.entrySet()) {
			int offset = tileWidth * cpt++;

			List<ClassROI> l = e.getValue();
			for (ClassROI r : l) {

				Rectangle rectangle2d = (Rectangle) r.shape.getBoundingRect();
				rectangle2d.translate(offset, 0);

				ShapeRoi newShapeRoi = new ShapeRoi(rectangle2d);
				ws.addExample(r.classe, newShapeRoi, 1);
			}

		}

		// wekaSegmentation.addExample(classNo, sr, 1);

		updateSegmentationParameters(ws);

		currentlyUsed = ws;

		currentlyUsed.trainClassifier();
	}

	@Override
	public BufferedImage apply(BufferedImage image) {

		ImagePlus ip = new ImagePlus();
		ip.setImage(image);

		Prefs.setThreads(1);
		ImagePlus result = currentlyUsed.applyClassifier(ip);

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

		ws.setClassBalance(true);

//		boolean[] enabledFeatures = ws.getEnabledFeatures();
//		for (int i = 0 ; i < enabledFeatures.length ; i ++) {
//		 	enabledFeatures[i] = true;
//		 }
//		
//		  enabledFeatures[FeatureStack.DERIVATIVES] = false;
//		 
//		  enabledFeatures[FeatureStack.GABOR] = true;
//		  enabledFeatures[FeatureStack.LIPSCHITZ] = true; //
//		  enabledFeatures[FeatureStack.HESSIAN] = true;
//		  enabledFeatures[FeatureStack.SOBEL] = true;
//		  enabledFeatures[FeatureStack.NEIGHBORS] = true;
//		  enabledFeatures[FeatureStack.GAUSSIAN] = true;
//		  
//		  enabledFeatures[FeatureStack.VARIANCE] = true;
//		  enabledFeatures[FeatureStack.STRUCTURE] = true;

//		  
//		  ws.setEnabledFeatures(enabledFeatures);
//		 
//		ws.updateClassifier(200, 6, 10);

	}

	private void constructTrainingSet(Instances instances, int classNo, IFamilyImageSeekerTiledImage familyTiledImage,
			Rectangle2D.Double rectangle) throws Exception {

		int height = familyTiledImage.getHeight();

		int startTrackPixel = (int) (rectangle.getY() / scale.getWidth() * height);
		int endTrackPixel = (int) ((rectangle.getY() + rectangle.getHeight()) / scale.getWidth() * height);

		int startPixel = (int) (rectangle.getX() / scale.getWidth() * height);
		int endPixel = (int) ((rectangle.getX() + rectangle.getWidth()) / scale.getWidth() * height);

		addPixelsToInstances(classNo, familyTiledImage, startTrackPixel, endTrackPixel, startPixel, endPixel);
	}

	private void constructTrainingSet(Hole hole, int classNo, IFamilyImageSeekerTiledImage familyTiledImage)
			throws Exception {

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

		addPixelsToInstances(classNo, familyTiledImage, startTrackPixel, endTrackPixel, startPixel, endPixel);
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
	private void addPixelsToInstances(int classNo, IFamilyImageSeekerTiledImage familyTiledImage, Rectangle2D rectangle)
			throws Exception {

		int x = (int) rectangle.getX();
		int y = (int) rectangle.getY();
		int width = (int) rectangle.getWidth();
		int height = (int) rectangle.getHeight();

		addPixelsToInstances(classNo, familyTiledImage, y, y + height, x, x + width);
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
	private void addPixelsToInstances(int classNo, IFamilyImageSeekerTiledImage familyTiledImage, int startTrackPixel,
			int endTrackPixel, int startPixel, int endPixel) throws Exception {

		int imageTileWidth = familyTiledImage.getTileWidth();

		int startImageIndex = startPixel / imageTileWidth;

		int endImageIndex = endPixel / imageTileWidth;
		assert startImageIndex <= endImageIndex;

		for (int i = startImageIndex; i <= endImageIndex; i++) {
			logger.debug("loading image :" + i); //$NON-NLS-1$

			// get associated WekeSegmentation

			List<ClassROI> l = perTileShapes.get(startImageIndex);

			if (l == null) {
				l = new ArrayList<>();
				perTileShapes.put(startImageIndex, l);
			}

			int offsetStart = startPixel - (i * imageTileWidth);
			int offsetEnd = endPixel - (i * imageTileWidth);

			Rectangle r = new Rectangle(offsetStart, startTrackPixel, offsetEnd - offsetStart,
					endTrackPixel - startTrackPixel);

			ShapeRoi sr = new ShapeRoi(r);

			l.add(new ClassROI(classNo, sr));

		}
	}

}
