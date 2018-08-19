package org.barrelorgandiscovery.gui.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.net.URL;

import org.barrelorgandiscovery.tools.ImageTools;

public class CursorTools {

	public static Cursor createCursorWithImage(BufferedImage smallImage)
			throws Exception {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension dim = kit.getBestCursorSize(32, 32);

		BufferedImage buffered = createCompatibleTranslucentImage(dim.width,
				dim.height);

		URL icon = CursorTools.class.getResource("aprintcursor.png");
		BufferedImage image = ImageTools.loadImage(kit.createImage(icon));

		int smallImageWidth = dim.width / 2;

		Graphics2D g = buffered.createGraphics();

		g.drawImage(image, 0, 0, 16, 16, null);
		if (smallImage != null) {
			g.drawImage(smallImage, smallImageWidth, smallImageWidth,
					smallImageWidth, smallImageWidth, null);
		}
		g.dispose();
		Cursor cursor = kit.createCustomCursor(buffered, new Point(0, 0),
				"myFancyCursor");
		return cursor;
	}

	private static boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

	/**
	 * <p>
	 * Returns a new translucent compatible image of the specified width and
	 * height. That is, the returned <code>BufferedImage</code> is compatible
	 * with the graphics hardware. If the method is called in a headless
	 * environment, then the returned BufferedImage will be compatible with the
	 * source image.
	 * </p>
	 * 
	 * @see #createCompatibleImage(java.awt.image.BufferedImage)
	 * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
	 * @see #createCompatibleImage(int, int)
	 * @see #loadCompatibleImage(java.net.URL)
	 * @see #toCompatibleImage(java.awt.image.BufferedImage)
	 * @param width
	 *            the width of the new image
	 * @param height
	 *            the height of the new image
	 * @return a new translucent compatible <code>BufferedImage</code> of the
	 *         specified width and height
	 */
	public static BufferedImage createCompatibleTranslucentImage(int width,
			int height) {
		return isHeadless() ? new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB) : getGraphicsConfiguration()
				.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
	}

	// Returns the graphics configuration for the primary screen
	private static GraphicsConfiguration getGraphicsConfiguration() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
	}

}
