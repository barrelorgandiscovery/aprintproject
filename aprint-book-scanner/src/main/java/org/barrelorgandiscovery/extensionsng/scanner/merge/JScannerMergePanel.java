package org.barrelorgandiscovery.extensionsng.scanner.merge;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.scanner.FamilyImageFolder;
import org.barrelorgandiscovery.extensionsng.scanner.IFamilyImageSeeker;
import org.barrelorgandiscovery.extensionsng.scanner.OpenCVJavaConverter;
import org.barrelorgandiscovery.extensionsng.scanner.OpenCVVideoFamilyImageSeeker;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesCalculation;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesCalculation.KeyPointMatch;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.math.MathVect;
import org.barrelorgandiscovery.prefs.AbstractFileObjectPrefsStorage;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JImageDisplayLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JShapeLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JShapeLayer.IShapeDrawer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.LayerChangedListener;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.CreatePointTool;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.IAnchorPointAdjuster;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.JViewingToolBar;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * Panel for constructing a unique image from regular images
 *
 * @author pfreydiere
 */
public class JScannerMergePanel extends JPanel implements Disposable {

	/** */
	private static final long serialVersionUID = -7007011059478439720L;

	private Logger logger = Logger.getLogger(JScannerMergePanel.class);

	IFamilyImageSeeker perfoScanFolder;

	JDisplay workImageDisplay;

	/** preview of result image **/
	JDisplay resultImageDisplay;

	private JImageDisplayLayer resultImageLayer;

	private JImageDisplayLayer image1Layer;

	private JCheckBox overlapLock;

	private JSpinner overlappixelsspinner;

	private JSlider currentResultImageSlider;

	private JShapeLayer<Rectangle2D.Double> workImageShapePointsLayer;

	private JShapeLayer<Shape> matriceShapesLayer;

	private IPrefsStorage preferences;

	private ExecutorService executor = Executors.newFixedThreadPool(3);
	private AtomicReference<ICancelTracker> currentProcessing = new AtomicReference<ICancelTracker>(null);

	/**
	 * Constructor
	 * 
	 * @param perfoScanFolder
	 * @param preferences
	 * @throws Exception
	 */
	public JScannerMergePanel(IFamilyImageSeeker perfoScanFolder, IPrefsStorage preferences) throws Exception {
		assert perfoScanFolder != null;
		this.perfoScanFolder = perfoScanFolder;
		assert preferences != null;
		this.preferences = preferences;
		initComponent();
	}

	@Override
	public void dispose() {
		// free associated resources
		executor.shutdown();
	}

	protected void initComponent() throws Exception {
		setLayout(new BorderLayout());

		Loader.load(opencv_java.class);
		
		FormPanel fp = new FormPanel(getClass().getResourceAsStream("mergepanel.jfrm"));

		fp.getLabel("positionning").setText("Positionning Elements");
		fp.getLabel("previewresult").setText("Preview Result");

		overlappixelsspinner = new JSpinner(new SpinnerNumberModel(52.0, 20.0, 300.0, 0.1));

		FormAccessor formAccessorParameters = fp.getFormAccessor("parameters");

		JLabel overlapppixelslabel = formAccessorParameters.getLabel("overlappixelslabel");
		overlapppixelslabel.setText("Overlap Pixels");

		formAccessorParameters.replaceBean("overlappixels", overlappixelsspinner);

		overlapLock = new JCheckBox("Lock Overlap to resolution");
		formAccessorParameters.replaceBean("overlaplock", overlapLock);

		// load and save parameters
		AbstractButton saveparameters = formAccessorParameters.getButton("saveparameters");
		saveparameters.setText("Save Parameters ...");
		saveparameters.addActionListener((e) -> {
			try {

				APrintFileChooser fc = new APrintFileChooser();
				int result = fc.showSaveDialog(this);
				if (result == APrintFileChooser.APPROVE_OPTION) {

					AbstractFileObject selectedFile = fc.getSelectedFile();
					if (selectedFile != null) {
						IPrefsStorage filePrefsStorage = new AbstractFileObjectPrefsStorage(selectedFile);
						saveModel(filePrefsStorage);
					}
				}

			} catch (Exception ex) {
				logger.error("error in saving parameters :" + ex.getMessage(), ex);
				JMessageBox.showError(this, ex);
			}
		});

		AbstractButton loadparameters = formAccessorParameters.getButton("loadparameters");
		loadparameters.setText("Load Parameters ...");
		loadparameters.addActionListener((e) -> {
			try {

				APrintFileChooser fc = new APrintFileChooser();
				int result = fc.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {

					AbstractFileObject selectedFile = fc.getSelectedFile();
					if (selectedFile != null) {
						IPrefsStorage filePrefsStorage = new AbstractFileObjectPrefsStorage(selectedFile);
						filePrefsStorage.load();
						loadModel(filePrefsStorage);
					}
				}

			} catch (Exception ex) {
				logger.error("error in saving parameters :" + ex.getMessage(), ex);
				JMessageBox.showError(this, ex);
			}
		});

		AbstractButton btnautoparameters = formAccessorParameters.getButton("btnautoparameters");
		btnautoparameters.setText("Auto Detect parameters");
		btnautoparameters.addActionListener((e) -> {
			try {

				int current = currentResultImageSlider.getValue();
				int next = current + 1;
				if (next >= currentResultImageSlider.getMaximum()) {
					JMessageBox.showMessage(this, "cannot compute parmeters for the latest image");
					return;
				}
				BufferedImage i1 = perfoScanFolder.loadImage(current);
				BufferedImage i2 = perfoScanFolder.loadImage(next);

				int reductedFactor = 8;
				
				Mat m1 = new Mat();
				OpenCVJavaConverter.toOpenCV(i1, m1);
				Mat rm1 = new Mat();
				Imgproc.resize(m1, rm1, new Size(m1.width() / reductedFactor ,m1.height() / reductedFactor));
				
				Mat m2 = new Mat();
				OpenCVJavaConverter.toOpenCV(i2, m2);
				Mat rm2 = new Mat();
				Imgproc.resize(m2, rm2, new Size(m2.width() / reductedFactor ,m2.height() / reductedFactor));
			
				List<KeyPointMatch> goodMatches = BooksImagesCalculation.detectKeyPoint(rm1, rm2);

				// knn the displacement vector
				KMeansPlusPlusClusterer<KeyPointMatch> kpm = new KMeansPlusPlusClusterer<KeyPointMatch>(10, 1000,
						new DistanceMeasure() {
							@Override
							public double compute(double[] a, double[] b) throws DimensionMismatchException {
								return Math.abs(Math.atan2(a[1], a[0]) - Math.atan2(b[1], b[0]));
							}
						});
				List<CentroidCluster<KeyPointMatch>> resultClusterDisplacement = kpm.cluster(goodMatches);

				if (resultClusterDisplacement.size() <= 0) {
					JMessageBox.showMessage(this, "cannot classify image");
					return;
				}
				
				CentroidCluster<KeyPointMatch> centroidCluster = resultClusterDisplacement.get(0);
				DoublePoint center = (DoublePoint)centroidCluster
						.getCenter();
				
				MathVect displacement = new MathVect(center.getPoint()[0], center.getPoint()[1]);
				overlappixelsspinner.setValue(displacement.norme() * reductedFactor);
				repaint();
				
			} catch (Exception ex) {
				logger.error("error in saving parameters :" + ex.getMessage(), ex);
				JMessageBox.showError(this, ex);
			}
		});

		// construct result
		resultImageDisplay = new JDisplay();
		resultImageLayer = new JImageDisplayLayer();
		resultImageDisplay.addLayer(resultImageLayer);

		workImageDisplay = new JDisplay();

		image1Layer = new JImageDisplayLayer();
		workImageDisplay.addLayer(image1Layer);

		int folderimagecount = perfoScanFolder.getImageCount();

		FormAccessor resultfunctions = fp.getFormAccessor("resultfunctions");

		currentResultImageSlider = (JSlider) resultfunctions.getComponentByName("currentresultimage");
		currentResultImageSlider.setValue(1);

		currentResultImageSlider.setMaximum(folderimagecount);
		currentResultImageSlider.setMajorTickSpacing(150);
		currentResultImageSlider.setMinorTickSpacing(50);
		currentResultImageSlider.revalidate();
		currentResultImageSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					setCurrentImage(currentResultImageSlider.getValue());
					recomputeResult();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		});

		final JShapeLayer<Polygon> zoneLayer = new JShapeLayer<>();

		workImageShapePointsLayer = new JShapeLayer<>();
		workImageShapePointsLayer.setGraphicsDrawer(new IShapeDrawer() {
			@Override
			public void draw(Shape s, Graphics2D g2d) {
				Color old = g2d.getColor();
				try {
					g2d.setColor(Color.CYAN);
					g2d.draw(s);
				} finally {
					g2d.setColor(old);
				}
			}
		});

		workImageShapePointsLayer.addLayerChangedListener(new LayerChangedListener() {

			@Override
			public void layerSelectionChanged() {
			}

			@Override
			public void layerContentChanged() {

				// construct associated zone
				zoneLayer.clear();
				Polygon p = new Polygon();
				List<Double> graphics = workImageShapePointsLayer.getGraphics();
				for (Shape s : graphics) {
					Rectangle2D.Double d = (Rectangle2D.Double) s;
					p.addPoint((int) d.getCenterX(), (int) d.getCenterY());
				}

				zoneLayer.add(p);
				updateModel();
				recomputeResult();
			}
		});
		workImageDisplay.addLayer(workImageShapePointsLayer);
		workImageDisplay.addLayer(zoneLayer);

		matriceShapesLayer = new JShapeLayer<>();
		workImageDisplay.addLayer(matriceShapesLayer);
		matriceShapesLayer.setGraphicsDrawer(new IShapeDrawer() {
			@Override
			public void draw(Shape s, Graphics2D g2d) {
				Color c = g2d.getColor();
				try {
					g2d.setColor(Color.yellow);
					g2d.draw(s);
				} finally {
					g2d.setColor(c);
				}
			}
		});

		CreatePointTool createPointTool = new CreatePointTool(workImageDisplay, workImageShapePointsLayer, 3);
		createPointTool.setAnchorPointAdjuster(new IAnchorPointAdjuster() {
			@Override
			public <T extends Shape> void adjust(List<T> shapes, Set<T> selectedShape, MathVect displacement) {

				// origin point
				Rectangle2D.Double p1 = (Rectangle2D.Double) shapes.get(0);

				// angle point
				Rectangle2D.Double p2 = (Rectangle2D.Double) shapes.get(1);
				// distance point
				Rectangle2D.Double p3 = (Rectangle2D.Double) shapes.get(2);

				if (selectedShape.size() != 1) {
					return; // multiple modifications, don't allow modifications
				}

				// adjust points
				Rectangle2D.Double p = (Rectangle2D.Double) selectedShape.iterator().next();
				if (p == p1) {
					// origin displacement
					p2.setRect(p2.getX() + displacement.getX(), p2.getY() + displacement.getY(), p2.getWidth(),
							p2.getHeight());

					p3.setRect(p3.getX() + displacement.getX(), p3.getY() + displacement.getY(), p3.getWidth(),
							p3.getHeight());
				}

				if (p == p2) {
					// angle displacement

					// rotate the length also

					// compute rotation
					MathVect mp2 = new MathVect(p2.x, p2.y);
					MathVect mp1 = new MathVect(p1.x, p1.y);
					MathVect v1 = mp2.moins(mp1);
					MathVect v2 = displacement.plus(mp2).moins(mp1);

					double angle = v2.angle(v1);

					MathVect mp3 = new MathVect(p3.x, p3.y);
					MathVect final3 = mp3.moins(mp1).rotate(-angle).plus(mp1);

					p3.setRect(final3.getX(), final3.getY(), p3.getWidth(), p3.getHeight());
				}

				if (p == p3 && overlapLock.isSelected()) {
					// prop, delta

					MathVect newdist = new MathVect(p3.getX() - p1.getX(), p3.getY() - p1.getY());
					MathVect oldDist = newdist.moins(displacement);

					Number n = (Number) overlappixelsspinner.getValue();
					double d = n.doubleValue();

					double newD = /* old */
							(1.0 - (newdist.norme() - oldDist.norme()) / oldDist.norme()) * d;
					overlappixelsspinner.setValue(newD);
				}
			}
		});

		fp.getFormAccessor().replaceBean("displaypositionning", workImageDisplay);

		// add toolbar for 1
		JViewingToolBar tb1 = new JViewingToolBar(workImageDisplay);
		JToggleButton tbpointtool = tb1.addTool(createPointTool);
		tbpointtool.setIcon(new ImageTools().loadIcon(createPointTool.getClass(), "kedit.png"));

		workImageDisplay.setCurrentTool(createPointTool);

		fp.getFormAccessor().replaceBean("positionningimagetoolbar", tb1);

		add(fp, BorderLayout.CENTER);

		// resultImageDisplay.setPreferredSize(new Dimension(400, 300));
		fp.getFormAccessor().replaceBean("displayresult", resultImageDisplay);

		JViewingToolBar tb2 = new JViewingToolBar(resultImageDisplay);

		fp.getFormAccessor().replaceBean("toolbarResults", tb2);

		AbstractButton saveresult = fp.getButton("saveresult");
		saveresult.setText("Save Images ...");
		saveresult.addActionListener((e) -> {
			try {

				File preferenceStorage = preferences.getFileProperty("mergescannerfolder",
						new File(System.getProperties().getProperty("user.home")));

				JFileChooser f = new JFileChooser(preferenceStorage);

				int result = f.showSaveDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File savedFile = f.getSelectedFile();
					if (savedFile != null) {
						logger.debug("saving images");
						saveWholeImage(savedFile);
						preferences.setFileProperty("mergescannerfolder", savedFile.getParentFile());
					}
				}

			} catch (Exception ex) {
				logger.error("error in saving files :" + ex.getMessage(), ex);
				JMessageBox.showError(this, ex);
			}
		});

		///////////////////////////////////////////////////////////////

		overlappixelsspinner.addChangeListener((e) -> {
			updateModel();
			logger.debug("model updated, recompute the result");
			try {
				recomputeResult(); // this update the model
			} catch (Throwable t) {
				logger.error("error while recomputing the model :" + t.getMessage(), t);
			}
		});

		workImageShapePointsLayer.add(new Rectangle2D.Double(100, 100, 10, 10));
		workImageShapePointsLayer.add(new Rectangle2D.Double(100, 150, 10, 10));
		workImageShapePointsLayer.add(new Rectangle2D.Double(300, 100, 10, 10));

		updateModel();
	}

	private ImageBookMergeModel model;

	/** update internal model from the gui elements */
	private void updateModel() {

		List<Double> graphics = workImageShapePointsLayer.getGraphics();

		if (graphics.size() < 3) {
			return;
		}

		Rectangle2D.Double p1 = (Rectangle2D.Double) graphics.get(0);
		Rectangle2D.Double p2 = (Rectangle2D.Double) graphics.get(1);
		Rectangle2D.Double p3 = (Rectangle2D.Double) graphics.get(2);

		ImageBookMergeModel mm = new ImageBookMergeModel();
		mm.origin = new Point2D.Double(p1.getCenterX(), p1.getCenterY());
		mm.pointforAngleAndImageWidth = new Point2D.Double(p2.getCenterX(), p2.getCenterY());
		mm.pointforBookWidth = new Point2D.Double(p3.getCenterX(), p3.getCenterY());

		try {
			updateDrawing(mm.origin, mm.pointforAngleAndImageWidth, mm.pointforBookWidth);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
		model = mm;
		model.overlappDistance = ((Number) overlappixelsspinner.getValue()).doubleValue();
	}

	/**
	 * update automatic draws from anchor points
	 * 
	 * @param origin
	 * @param anglePoint
	 * @param distancePoint
	 */
	private void updateDrawing(Point2D.Double origin, Point2D.Double anglePoint, Point2D.Double distancePoint) {
		matriceShapesLayer.clear();

		MathVect originVect = new MathVect(origin.x, origin.y);
		MathVect angleVect = new MathVect(anglePoint.x, anglePoint.y).moins(originVect);
		MathVect distanceVect = new MathVect(distancePoint.x, distancePoint.y).moins(originVect);

		// draw rectangle

		Path2D.Double p = new Path2D.Double();
		p.moveTo(originVect.getX(), originVect.getY());

		// draw to angle point
		p.lineTo(anglePoint.x, anglePoint.y);

		MathVect topVect = angleVect.rotate(-Math.PI / 2).orthoNorme().scale(distanceVect.norme());
		MathVect topPos = topVect.plus(angleVect).plus(originVect);
		p.lineTo(topPos.getX(), topPos.getY());

		topPos = topPos.moins(angleVect);
		p.lineTo(topPos.getX(), topPos.getY());
		topPos = originVect;
		p.lineTo(topPos.getX(), topPos.getY());
		p.closePath();

		matriceShapesLayer.add(p);

		// draw arrow for book move

		Path2D.Double p2 = new Path2D.Double();
		MathVect mid = topVect.scale(0.5).plus(originVect);
		MathVect startMid = mid.plus(angleVect.scale(0.1));
		MathVect midL = mid.plus(angleVect.scale(0.9));

		MathVect arrowEl = angleVect.scale(0.2);

		p2.moveTo(midL.getX(), midL.getY());
		p2.lineTo(startMid.getX(), startMid.getY());
		p2.lineTo(startMid.plus(arrowEl.rotate(0.2)).getX(), startMid.plus(arrowEl.rotate(0.2)).getY());
		p2.lineTo(startMid.getX(), startMid.getY());
		p2.lineTo(startMid.plus(arrowEl.rotate(-0.2)).getX(), startMid.plus(arrowEl.rotate(-0.2)).getY());

		matriceShapesLayer.add(p2);

		p = new Path2D.Double();
		double radius = 20.0;
		// draw Origin circle
		for (int i = 0; i <= 100; i++) {
			double x = originVect.getX() + radius * Math.cos(i * Math.PI * 2 / 100);
			double y = originVect.getY() + radius * Math.sin(i * Math.PI * 2 / 100);
			if (i == 0) {
				p.moveTo(x, y);
			} else {
				p.lineTo(x, y);
			}
		}
		p.closePath();

		matriceShapesLayer.add(p);

	}

	public void setCurrentImage(int i) throws Exception {
		BufferedImage i1 = perfoScanFolder.loadImage(i);
		image1Layer.setImageToDisplay(i1);
		resultImageLayer.setImageToDisplay(i1);

		workImageDisplay.repaint();
		resultImageDisplay.repaint();
	}

	final int FINAL_IMAGE_HEIGHT = 1200;
	final int FINAL_IMAGE_WIDTH = 1600;

	private class ComputeResultImage implements Runnable {

		private ICancelTracker cancelTracker;
		private Function<ICancelTracker, BufferedImage> f;

		public ComputeResultImage(ICancelTracker cancelTracker, Function<ICancelTracker, BufferedImage> f) {
			this.cancelTracker = cancelTracker;
			this.f = f;
		}

		@Override
		public void run() {

			BufferedImage image = f.apply(cancelTracker);
			if (image != null && !cancelTracker.isCanceled()) {
				resultImageLayer.setImageToDisplay(image);
				resultImageDisplay.repaint();
			}
		}
	}

	/** recompute the result in the result window */
	private void recomputeResult() {

		List<Double> graphics = workImageShapePointsLayer.getGraphics();

		if (graphics.size() >= 3) {
			// Construct transform
			updateModel();

			// compute
			MathVect m = model.getAngleAndImageWidthVector();

			int pixelsForEachImage = (int) m.norme();
			if (pixelsForEachImage < 30)
				pixelsForEachImage = 30;

			// overlap value
			Number o = (Number) overlappixelsspinner.getValue();
			int start = currentResultImageSlider.getValue();

			final int finalpixelsforeachimage = pixelsForEachImage;
			ICancelTracker currentCancelTracker = new CancelTracker();
			ICancelTracker old = currentProcessing.getAndSet(currentCancelTracker);
			if (old != null) {
				old.cancel();
			}

			executor.execute(new ComputeResultImage(currentCancelTracker, (c) -> {
				try {

					ClassLoader ctxcl = Thread.currentThread().getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(JScannerMergePanel.class.getClassLoader());
						return constructMergeImage(finalpixelsforeachimage, FINAL_IMAGE_WIDTH, o, start, start + 20, 0,
								c);

					} finally {
						Thread.currentThread().setContextClassLoader(ctxcl);
					}

				} catch (Exception ex) {
					logger.error("" + ex.getMessage(), ex);
					return null;
				}
			}));
		}
	}

	/**
	 * internal method to construct a result image
	 *
	 * @param pixelsForEachImage
	 * @param output_final_image_width
	 * @param overlapPixels
	 * @param startImage
	 * @param lastImage
	 * @param offsetForFirstImage
	 * @return
	 * @throws Exception
	 */
	private BufferedImage constructMergeImage(int pixelsForEachImage, int output_final_image_width,
			Number overlapPixels, int startImage, int lastImage, int offsetForFirstImage, ICancelTracker cancelTracker)
			throws Exception {

		// clone the model
		final ImageBookMergeModel innerModel = (ImageBookMergeModel) org.barrelorgandiscovery.extensionsng.scanner.wizard.scanormerge.SerializeTools
				.deepClone(model);

		// RAZ
		int NBCOMPONENTS = 4;
		int[][][] fullImage = new int[output_final_image_width][FINAL_IMAGE_HEIGHT][NBCOMPONENTS];
		int[][] nbpixels = new int[output_final_image_width][FINAL_IMAGE_HEIGHT];
		for (int x = 0; x < output_final_image_width; x++) {
			for (int y = 0; y < FINAL_IMAGE_HEIGHT; y++) {
				for (int j = 0; j < NBCOMPONENTS; j++) {
					fullImage[x][y][j] = 0;
				}
				nbpixels[x][y] = 0;
			}
		}

		for (int j = startImage; j < lastImage; j++) {

			if (cancelTracker != null && cancelTracker.isCanceled()) {
				return null; // aborted
			}

			/*
			 * File f = perfoScanFolder.constructImageFile(j); if (!f.exists()) { continue;
			 * }
			 */
			BufferedImage current = perfoScanFolder.loadImage(j);

			BufferedImage newImage = innerModel.createSlice(current, pixelsForEachImage, FINAL_IMAGE_HEIGHT);

			Raster sliceData = newImage.getData();
			for (int x = 0; x < newImage.getWidth(); x++) {
				int xpixel = (int) (x + (j - startImage) * overlapPixels.doubleValue() + offsetForFirstImage);

				if (xpixel > output_final_image_width - 1)
					continue;
				if (xpixel < 0)
					continue;

				for (int y = 0; y < newImage.getHeight(); y++) {

					// get pixel value
					int[] values = new int[NBCOMPONENTS];
					sliceData.getPixel(x, y, values);

					for (int i = 0; i < NBCOMPONENTS; i++) {
						fullImage[xpixel][y][i] += values[i];
					}
					nbpixels[xpixel][y] += 1;
				}
			}
		} // for

		// construct result image, making means on pixel values (rgb)

		BufferedImage image = new BufferedImage(output_final_image_width, FINAL_IMAGE_HEIGHT,
				BufferedImage.TYPE_4BYTE_ABGR);
		WritableRaster imageRaster = image.getRaster();

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {

				if (nbpixels[x][y] > 0) {
					int r = fullImage[x][y][0] / nbpixels[x][y];
					int g = fullImage[x][y][1] / nbpixels[x][y];
					int b = fullImage[x][y][2] / nbpixels[x][y];

					imageRaster.setPixels(x, y, 1, 1, new int[] { r, g, b, 255 });
				}
			}
		}
		return image;
	}

	/**
	 * save result image in folder, to be able to display it
	 *
	 * @param destinationFolder
	 * @throws Exception
	 */
	public void saveImage(File destinationFolder) throws Exception {
		assert destinationFolder != null;
		assert destinationFolder.exists();
		assert destinationFolder.isDirectory();

		boolean endreach = false;
		int currentimageindex = 0;

		while (!endreach) {
			logger.debug("writing image " + currentimageindex);
			double l = currentimageindex * FINAL_IMAGE_HEIGHT;
			int indexFirstImage = (int) Math.ceil(l / model.overlappDistance);
			indexFirstImage -= (int) Math.floor(FINAL_IMAGE_HEIGHT / model.getAngleAndImageWidthVector().norme());
			int lastIndexImage = (int) Math.floor((l + FINAL_IMAGE_HEIGHT) / model.overlappDistance);

			BufferedImage img = constructMergeImage((int) model.getAngleAndImageWidthVector().norme(),
					FINAL_IMAGE_HEIGHT, model.overlappDistance, indexFirstImage, indexFirstImage + 30,
					(int) ((indexFirstImage) * model.overlappDistance - l), null);

			ImageIO.write(img, "JPEG", new File(destinationFolder, "" + currentimageindex + ".jpg"));

			if (lastIndexImage >= perfoScanFolder.getImageCount()) {
				endreach = true;
			}
			currentimageindex++;
		}
	}

	/**
	 * Save the image on one big image
	 *
	 * @param folder
	 * @throws Exception
	 */
	public void saveWholeImage(File outfile) throws Exception {
		int width = (int) (model.overlappDistance * perfoScanFolder.getImageCount()
				+ model.getAngleAndImageWidthVector().norme());

		BufferedImage img = constructMergeImage((int) model.getAngleAndImageWidthVector().norme(), width,
				model.overlappDistance, 0, perfoScanFolder.getImageCount(), 0, null);

		ImageIO.write(img, "JPEG", outfile);
	}

	/**
	 * load a parameter model and apply it to the current frame
	 *
	 * @param storage
	 */
	public void loadModel(IPrefsStorage storage) {
		assert storage != null;
		logger.debug("load from storage " + storage);
		model.loadFrom(storage);
		// update the ui

		List<Double> graphics = workImageShapePointsLayer.getGraphics();
		moveRectTo(graphics.get(0), model.origin);
		moveRectTo(graphics.get(1), model.pointforAngleAndImageWidth);
		moveRectTo(graphics.get(2), model.pointforBookWidth);
		overlappixelsspinner.setValue(model.overlappDistance);
		workImageShapePointsLayer.signalLayerContentChanged();
	}

	private void moveRectTo(Rectangle2D.Double r, Point2D p) {
		r.setRect(p.getX() - 10 / 2, p.getY() - 10 / 2, 10, 10);
	}

	public void saveModel(IPrefsStorage storage) {
		assert storage != null;
		logger.debug("save to storage " + storage);
		model.saveTo(storage);
	}

	private static void testWithImageFolder() throws Exception {
		// File scanfolder = new File("C:\\temp\\perfo20180721");

		// File scanfolder = new
		// File("C:\\projets\\APrint\\contributions\\patrice\\2018_numerisation_josephine");

		// File scanfolder =
		// new File(
		// "C:\\projets\\APrint\\contributions\\patrice\\2018_numerisation_machine_faiscapourmoi\\perfo");

		File scanfolder = new File("C:\\projets\\APrint\\contributions\\plf\\2019-07-19_scan_video_popcorn_52");
		FamilyImageFolder perfoScanFolder = new FamilyImageFolder(scanfolder, Pattern.compile("exported.*"));

		JFrame f = new JFrame();

		f.setSize(800, 800);

		f.getContentPane().setLayout(new BorderLayout());

		FilePrefsStorage p = new FilePrefsStorage(new File("c:\\temp\\preferencesStorage.properties"));
		p.load();

		JScannerMergePanel scanMergePanel = new JScannerMergePanel(perfoScanFolder, p);
		f.getContentPane().add(scanMergePanel, BorderLayout.CENTER);

		scanMergePanel.setCurrentImage(1);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		f.setVisible(true);
	}

	private static void testWithVideo() throws Exception {

		File scanfolder = new File(
				"C:\\projets\\APrint\\contributions\\plf\\2020-10_Essais saisie video\\carton ancien avec fond blanc.mp4");
		OpenCVVideoFamilyImageSeeker perfoScanFolder = new OpenCVVideoFamilyImageSeeker(scanfolder, 2, 5);

		JFrame f = new JFrame();

		f.setSize(800, 800);

		f.getContentPane().setLayout(new BorderLayout());

		FilePrefsStorage p = new FilePrefsStorage(new File("c:\\temp\\preferencesStorage_video.properties"));
		p.load();

		JScannerMergePanel scanMergePanel = new JScannerMergePanel(perfoScanFolder, p);
		f.getContentPane().add(scanMergePanel, BorderLayout.CENTER);

		scanMergePanel.setCurrentImage(1);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		f.setVisible(true);
	}

	/**
	 * Test method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Loader.load(opencv_java.class);
		testWithVideo();
	}
}
