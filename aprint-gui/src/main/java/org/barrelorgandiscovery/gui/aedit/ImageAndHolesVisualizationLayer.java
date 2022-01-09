package org.barrelorgandiscovery.gui.aedit;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import org.barrelorgandiscovery.bookimage.BookImage;
import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;
import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.images.books.tools.IFileBasedTiledImage;
import org.barrelorgandiscovery.images.books.tools.ITiledImage;
import org.barrelorgandiscovery.images.books.tools.StandaloneTiledImage;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Layer for visualizing background recognition image and associated holes
 * 
 * @author use
 * 
 */
public class ImageAndHolesVisualizationLayer implements VirtualBookComponentBackgroundLayer {

	/**
	 * the layer is visible ?
	 */
	private boolean visible = true;

	/**
	 * background recognition image (standalone)
	 */
	private BufferedImage backgroundimage = null;

	/**
	 * tiledimage
	 */
	private ITiledImage tileImage = null;

	/**
	 * offset of the image and holes
	 */
	private double xoffset = 0;

	private double xscale = 1.0;

	/**
	 * displayed holes
	 */
	private ArrayList<Hole> holes = null;

	/**
	 * Display Shapes
	 */
	private ArrayList<Shape> shapes = new ArrayList<>();

	/**
	 * flip the image display
	 */
	private boolean flipHorizontallyTheImage = false;

	private boolean disableRescale = false;

	/**
	 * specific holes drawing color, red by default
	 */
	private Color holesColor = Color.red;

	private Stroke holesStroke = null;

	private String layerInternalName = null;

	public void setLayerInternalName(String layerInternalName) {
		this.layerInternalName = layerInternalName;
	}

	public String getLayerInternalName() {
		return layerInternalName;
	}

	public void setHolesColor(Color holesColor) {
		if (holesColor != null)
			this.holesColor = holesColor;
	}

	public Color getHolesColor() {
		return holesColor;
	}

	public void setHolesStroke(Stroke holesStroke) {
		this.holesStroke = holesStroke;
	}

	public Stroke getHolesStroke() {
		return holesStroke;
	}

	public void setDisableRescale(boolean disableRescale) {
		this.disableRescale = disableRescale;
	}

	public boolean isDisableRescale() {
		return disableRescale;
	}

	/**
	 * setter for the image
	 * 
	 * @param backgroundimage
	 */
	public void setBackgroundimage(BufferedImage backgroundimage) {
		this.backgroundimage = backgroundimage;
	}

	public BufferedImage getBackgroundimage() {
		return backgroundimage;
	}

	public void setTiledBackgroundimage(ITiledImage backgroundimage) {
		this.tileImage = backgroundimage;
	}

	public ITiledImage getTiledBackgroundimage() {
		return tileImage;
	}

	public void setHoles(ArrayList<Hole> holes) {
		this.holes = holes;
	}

	public ArrayList<Hole> getHoles() {
		return holes;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	private float opacity = 1.0f;

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public float getOpacity() {
		return opacity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.VirtualBookComponentBackgroundLayer#
	 * drawBackground(java.awt.Graphics,
	 * org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent)
	 */
	public void drawBackground(Graphics g, JVirtualBookComponent component) {
		if (!isVisible())
			return;

		Graphics2D g2d = (Graphics2D) g;
		Composite oldComposite = g2d.getComposite();
		try {

			AlphaComposite transparencyComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, opacity);
			g2d.setComposite(transparencyComposite);

			if (backgroundimage != null) {

				VirtualBook vb = component.getVirtualBook();

				if (vb == null)
					return;

				Scale s = vb.getScale();
				double width = s.getWidth();

				BufferedImage imageToDisplay = backgroundimage;

				AffineTransform shift = constructAffineTransformForDisplay(component, width,
						imageToDisplay.getHeight());
				g2d.drawImage(imageToDisplay, shift, component);

			}

			if (tileImage != null) {

				try {

					/////////// compute transform
					VirtualBook vb = component.getVirtualBook();

					Scale s = vb.getScale();
					double width = s.getWidth();
					double f = 1.0 * width / tileImage.getHeight() * component.MmToPixel(1000) / 1000;

					// f is the scale factor between the pixels and the book's width

					AffineTransform scaling = AffineTransform.getScaleInstance(f, f);
					AffineTransform xoff = AffineTransform.getTranslateInstance(
							component.MmToPixel(-component.getXoffset() + component.getMargin()),
							component.MmToPixel(-component.getYoffset() + component.getMargin()));

					if (!disableRescale) {
						xoff.concatenate(scaling);
					}

					// xoff is the transform from pixel to the screen display coordsys
					// it first transform the scale, then the translation

					// get the visible bounds
					Rectangle bounds = g2d.getClipBounds();
					Rectangle2D.Double r = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(),
							bounds.getHeight());

					AffineTransform inverse = xoff.createInverse();
					Shape invertedBox = inverse.createTransformedShape(r);

					// search the tiles associated to the inverted bounds
					int[] images = tileImage.subTiles((Rectangle2D.Double) invertedBox.getBounds2D());

					if (images != null) {
						currentVisibleTiles.set(images);
					}
					for (int i = 0; i < images.length; i++) {
						try {
							Double d = tileImage.subTileDimension(images[i]);

							AffineTransform t = AffineTransform.getTranslateInstance(d.getX(), 0);

							BufferedImage loadImage = null;

							if (tileImage instanceof StandaloneTiledImage) {
								loadImage = ((StandaloneTiledImage) tileImage).getImage();
							} else if (tileImage instanceof IFamilyImageSeeker) {
								loadImage = ((IFamilyImageSeeker) tileImage).loadImage(images[i]);
							} else if (tileImage instanceof IFileBasedTiledImage) {
								File filePath = ((IFileBasedTiledImage) tileImage).getImagePath(images[i]);
								if (filePath != null && filePath.exists()) {
									loadImage = ImageTools.loadImage(filePath.toURL());
								}
							} else if (tileImage instanceof BookImage) {
								loadImage = ((BookImage) tileImage).loadImage(images[i]);
							}

							if (loadImage != null) {

								AffineTransform m = AffineTransform.getTranslateInstance(1, 1);

								if (flipHorizontallyTheImage) {
									// loadImage = reverseImage(loadImage);

									// invert the image
									AffineTransform scaleTransform = AffineTransform.getScaleInstance(1, -1);
									AffineTransform translateInstance = AffineTransform.getTranslateInstance(0,
											loadImage.getHeight());

									translateInstance.concatenate(scaleTransform);
									// imageToDisplay = reverseImage(backgroundimage);
									m.concatenate(translateInstance);
								}

								AffineTransform scaling2 = AffineTransform.getScaleInstance(f, f);
								scaling2.concatenate(m);

								AffineTransform xoff2 = AffineTransform.getTranslateInstance(
										component.MmToPixel(-component.getXoffset() + component.getMargin()),
										component.MmToPixel(-component.getYoffset() + component.getMargin()));

								scaling2.concatenate(t);
								xoff2.concatenate(scaling2);

								// xoff2 is the transform between the pixel to screen

								g2d.drawImage(loadImage, xoff2, null);
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		} finally {
			g2d.setComposite(oldComposite);
		}

	}

	/**
	 * Construct affine transformation
	 * 
	 * @param component
	 * @param dimensionOnBook
	 * @param dimensionInOrigin
	 * @return
	 */
	private AffineTransform constructAffineTransformForDisplay(JVirtualBookComponent component, double dimensionOnBook,
			double dimensionInOrigin) {
		AffineTransform t = AffineTransform.getScaleInstance(1, 1);

		if (flipHorizontallyTheImage) {
			// invert the image
			AffineTransform scaleTransform = AffineTransform.getScaleInstance(1, -1);
			AffineTransform translateInstance = AffineTransform.getTranslateInstance(0, dimensionInOrigin);

			translateInstance.concatenate(scaleTransform);
			// imageToDisplay = reverseImage(backgroundimage);
			t.concatenate(translateInstance);
		}

		double factor = (1.0d * component.MmToPixel(dimensionOnBook) / dimensionInOrigin);
//				int iwidth = (int) (factor
//						* imageToDisplay.getWidth());
//				int iheight = component.MmToPixel(width);

		if (disableRescale) {
//					iwidth = imageToDisplay.getWidth();
//					iheight = imageToDisplay.getHeight();
			factor = 1.0d;
		}

		AffineTransform display = AffineTransform.getScaleInstance(factor, factor);
		display.concatenate(t);

		AffineTransform shift = AffineTransform.getTranslateInstance(component.convertCartonToScreenX(xoffset),
				component.convertCartonToScreenY(0));
		shift.concatenate(display);
		return shift;
	}

	private BufferedImage reverseImage(BufferedImage inputImage) {
		BufferedImage imageToDisplay;
		imageToDisplay = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
		Graphics2D g2d = imageToDisplay.createGraphics();
		try {
			g2d.drawImage(inputImage, 0, 0, inputImage.getWidth(), inputImage.getHeight(), 0, inputImage.getHeight(),
					inputImage.getWidth(), 0, null);
		} finally {
			g2d.dispose();
		}
		return imageToDisplay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer#draw(java.
	 * awt.Graphics, org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent)
	 */
	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference) {
		if (!isVisible())
			return;

		if (holes == null)
			return;

		VirtualBook vb = jbookcomponentreference.getVirtualBook();
		if (vb == null)
			return;

		Scale scale = vb.getScale();

		g.setColor(holesColor);
		g.setPaintMode();

		Graphics2D g2d = (Graphics2D) g;

		Stroke stroke = new BasicStroke(2.0f);
		if (this.holesStroke != null) {
			stroke = this.holesStroke;
		}
		g2d.setStroke(stroke);

		for (Iterator<Hole> iterator = holes.iterator(); iterator.hasNext();) {
			Hole h = iterator.next();

			double xmm = xoffset + jbookcomponentreference.timestampToMM(h.getTimestamp()) * xscale;
			double widthmm = jbookcomponentreference.timeToMM(h.getTimeLength()) * xscale;

			double y = scale.getFirstTrackAxis() + scale.getIntertrackHeight() * h.getTrack();

			if (scale.isPreferredViewedInversed())
				y = scale.getWidth() - y;

			y -= scale.getTrackWidth() / 2;

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));

			g2d.fillRect(jbookcomponentreference.convertCartonToScreenX(xmm),
					jbookcomponentreference.convertCartonToScreenY(y), jbookcomponentreference.MmToPixel(widthmm),
					jbookcomponentreference.MmToPixel(scale.getTrackWidth()));
		}

		AffineTransform a = constructAffineTransformForDisplay(jbookcomponentreference, scale.getWidth(),
				scale.getWidth());
		AffineTransform oldTransform = g2d.getTransform();
		try {
			g2d.setTransform(a);
			for (Shape s : shapes) {
				g2d.draw(s);
			}
			g2d.setPaintMode();

		} finally {
			g2d.setTransform(oldTransform);
		}

	}

	public void setXoffset(double xoffset) {
		this.xoffset = xoffset;
	}

	/**
	 * Image Offset in mm
	 * 
	 * @return
	 */
	public double getXoffset() {
		return xoffset;
	}

	public void setXscale(double xscale) {
		this.xscale = xscale;
	}

	public double getXscale() {
		return xscale;
	}

	public void setFlipHorizontallyTheImage(boolean flipHorizontallyTheImage) {
		this.flipHorizontallyTheImage = flipHorizontallyTheImage;
	}

	public boolean isFlipHorizontallyTheImage() {
		return flipHorizontallyTheImage;
	}

	private AtomicReference<int[]> currentVisibleTiles = new AtomicReference<int[]>(new int[0]);

	public int[] getCurrentVisibleTiles() {
		int[] c = currentVisibleTiles.get();
		assert c != null;
		return c;
	}

	public List<Shape> getAdditionalShapes() {
		return this.shapes;
	}

	public static void main(String[] args) throws Exception {

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(600, 800);

		f.getContentPane().setLayout(new BorderLayout());

		JVirtualBookScrollableComponent s = new JVirtualBookScrollableComponent();
		f.getContentPane().add(s, BorderLayout.CENTER);

		VirtualBook vb = new VirtualBook(Scale.getGammeMidiInstance());
		s.setVirtualBook(vb);

		ImageAndHolesVisualizationLayer l = new ImageAndHolesVisualizationLayer();
		l.setFlipHorizontallyTheImage(false);
//		BufferedImage i = ImageTools.loadImage(new File(
//				"C:\\projets\\APrint\\contributions\\plf\\2020-11-29_poncif_35\\1ereversion.bookimage.tiled\\14_recognized_inline.jpg"));
//		l.setBackgroundimage(i);

		ZipBookImage zbi = new ZipBookImage(new File(
				"C:\\projets\\APrint\\contributions\\plf\\2020-12-12_test_videos_image\\testpfr_constantinople.bookimage"));
		l.setTiledBackgroundimage(zbi);

		s.addLayer(l);

		f.setVisible(true);

		s.fitToScreen();

	}

}
