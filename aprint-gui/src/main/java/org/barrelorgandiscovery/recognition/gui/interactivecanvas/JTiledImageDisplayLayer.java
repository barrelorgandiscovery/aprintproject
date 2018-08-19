package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.recognition.gui.books.tools.ITiledImage;
import org.barrelorgandiscovery.tools.ImageTools;

public class JTiledImageDisplayLayer extends JLayer {

	private static final Logger logger = Logger.getLogger(JTiledImageDisplayLayer.class);
	
	private ITiledImage imageToDisplay = null;
	
	private JDisplay id = null;

	private java.lang.Double transparency;

	public JTiledImageDisplayLayer(JDisplay d) {
		this.id = d;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void drawLayer(Graphics2D g2d) {

		if (imageToDisplay != null) {

			Composite oldc = g2d.getComposite();
			try {
				if (transparency != null) {
					g2d.setComposite(
							AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) (double) transparency));
				}

				Rectangle bounds = g2d.getClipBounds();
				Rectangle2D.Double r = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(),
						bounds.getHeight());
				int[] images = imageToDisplay.subTiles(r);

				for (int i = 0; i < images.length; i++) {
					try {
						Double d = imageToDisplay.subTileDimension(images[i]);
						AffineTransform t = AffineTransform.getTranslateInstance(d.getX(), 0);
						File imagePath = imageToDisplay.getImagePath(images[i]);
						if (imagePath.exists()) {
							try {
								BufferedImage loadImage = ImageTools.loadImage(
										Toolkit.getDefaultToolkit().createImage(imagePath.getAbsolutePath()));
								g2d.drawImage(loadImage, t, id);
							} catch (Exception ex) {
								// image may be currently under writing
								// conditions,
								// ignore the exceptions
								// log it to avoid silent exceptions
								logger.debug(ex.getMessage(), ex);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			} finally {
				g2d.setComposite(oldc);
			}
		}
	}

	public void setImageToDisplay(ITiledImage imageToDisplay) {
		this.imageToDisplay = imageToDisplay;
	}

	public ITiledImage getImageToDisplay() {
		return imageToDisplay;
	}

	public void setTransparency(java.lang.Double transparency) {
		this.transparency = transparency;
	}

	public java.lang.Double getTransparency() {
		return transparency;
	}

	@Override
	public Rectangle2D.Double getExtent() {
		if (imageToDisplay == null)
			return null;

		Rectangle2D.Double r = new Rectangle2D.Double(0, 0, imageToDisplay.getWidth(), imageToDisplay.getHeight());
		return r;
	}

}
