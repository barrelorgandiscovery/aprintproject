package org.barrelorgandiscovery.gui.aedit;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.barrelorgandiscovery.bookimage.BookImage;
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
	 * background recognition image
	 */
	private BufferedImage backgroundimage = null;

	/**
	 * 
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
	 * flip the image display
	 */
	private boolean flipHorizontallyTheImage = false;

	private boolean disableRescale = false;

	/**
	 * specific holes drawing color, red by default
	 */
	private Color holesColor = Color.red;

	public void setHolesColor(Color holesColor) {
		if (holesColor != null)
			this.holesColor = holesColor;
	}

	public Color getHolesColor() {
		return holesColor;
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

		if (backgroundimage != null) {

			VirtualBook vb = component.getVirtualBook();

			if (vb == null)
				return;

			Scale s = vb.getScale();
			double width = s.getWidth();

			BufferedImage imageToDisplay = backgroundimage;
			if (flipHorizontallyTheImage) {
				imageToDisplay = reverseImage(backgroundimage);
			}

			int iwidth = (int) ((1.0 * component.MmToPixel(width) / imageToDisplay.getHeight())
					* imageToDisplay.getWidth());
			int iheight = component.MmToPixel(width);

			if (disableRescale) {
				iwidth = imageToDisplay.getWidth();
				iheight = imageToDisplay.getHeight();
			}

			g.drawImage(imageToDisplay, component.convertCartonToScreenX(xoffset), component.convertCartonToScreenY(0),
					iwidth, iheight, component);
		}

		if (tileImage != null) {

			try {
				Graphics2D g2d = (Graphics2D) g;

				/////////// compute transform
				VirtualBook vb = component.getVirtualBook();

				Scale s = vb.getScale();
				double width = s.getWidth();
				double f = 1.0 * width / tileImage.getHeight() * component.MmToPixel(1000) / 1000;
				AffineTransform scaling = AffineTransform.getScaleInstance(f, f);
				AffineTransform xoff = AffineTransform.getTranslateInstance(
						component.MmToPixel(-component.getXoffset() + component.getMargin()),
						component.MmToPixel(-component.getYoffset() + component.getMargin()));

				// scaling.concatenate(t);
				if (!disableRescale) {
					xoff.concatenate(scaling);
				}

				Rectangle bounds = g2d.getClipBounds();
				Rectangle2D.Double r = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(),
						bounds.getHeight());

				AffineTransform inverse = xoff.createInverse();
				Shape invertedBox = inverse.createTransformedShape(r);

				int[] images = tileImage.subTiles((Rectangle2D.Double) invertedBox.getBounds2D());

				for (int i = 0; i < images.length; i++) {
					try {
						Double d = tileImage.subTileDimension(images[i]);

						AffineTransform t = AffineTransform.getTranslateInstance(d.getX(), 0);

						BufferedImage loadImage = null;

						if (tileImage instanceof StandaloneTiledImage) {
							loadImage = ((StandaloneTiledImage) tileImage).getImage();
						} else if (tileImage instanceof IFileBasedTiledImage) {

							File filePath = ((IFileBasedTiledImage) tileImage).getImagePath(images[i]);
							if (filePath != null && filePath.exists()) {
								loadImage = ImageTools.loadImage(filePath.toURL());

							}
						} else if (tileImage instanceof BookImage) {
							loadImage = ((BookImage) tileImage).loadImage(images[i]);
						}

						if (loadImage != null) {

							if (flipHorizontallyTheImage) {
								loadImage = reverseImage(loadImage);
							}

							AffineTransform scaling2 = AffineTransform.getScaleInstance(f, f);
							AffineTransform xoff2 = AffineTransform.getTranslateInstance(
									component.MmToPixel(-component.getXoffset() + component.getMargin()),
									component.MmToPixel(-component.getYoffset() + component.getMargin()));

							scaling2.concatenate(t);
							xoff2.concatenate(scaling2);

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

		g2d.setStroke(new BasicStroke(2.0f));

		for (Iterator iterator = holes.iterator(); iterator.hasNext();) {
			Hole h = (Hole) iterator.next();

			double xmm = xoffset + jbookcomponentreference.timestampToMM(h.getTimestamp()) * xscale;
			double widthmm = jbookcomponentreference.timeToMM(h.getTimeLength()) * xscale;

			double y = scale.getFirstTrackAxis() + scale.getIntertrackHeight() * h.getTrack();

			if (scale.isPreferredViewedInversed())
				y = scale.getWidth() - y;

			y -= scale.getIntertrackHeight() / 2;

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));

			g2d.fillRect(jbookcomponentreference.convertCartonToScreenX(xmm),
					jbookcomponentreference.convertCartonToScreenY(y), jbookcomponentreference.MmToPixel(widthmm),
					jbookcomponentreference.MmToPixel(scale.getTrackWidth()));

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

}
