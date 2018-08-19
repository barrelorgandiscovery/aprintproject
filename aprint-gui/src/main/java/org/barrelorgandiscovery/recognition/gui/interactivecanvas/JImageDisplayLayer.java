package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class JImageDisplayLayer extends JLayer {

	private BufferedImage imageToDisplay = null;

	private Double transparency;

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

				g2d.drawImage(imageToDisplay, null, null);
			} finally {
				g2d.setComposite(oldc);
			}
		}

	}

	public void setTransparency(Double transparency) {
		this.transparency = transparency;
	}
	
	public Double getTransparency() {
		return transparency;
	}
	
	public void setImageToDisplay(BufferedImage imageToDisplay) {
		this.imageToDisplay = imageToDisplay;
	}

	public BufferedImage getImageToDisplay() {
		return imageToDisplay;
	}

	@Override
	public Rectangle2D.Double getExtent() {
		if (imageToDisplay == null)
			return null;

		Rectangle2D.Double r = new Rectangle2D.Double(0, 0, imageToDisplay.getWidth(), imageToDisplay.getHeight());
		return r;
	}

}
