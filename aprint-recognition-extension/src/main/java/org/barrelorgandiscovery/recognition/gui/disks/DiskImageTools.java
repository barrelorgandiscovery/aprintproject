package org.barrelorgandiscovery.recognition.gui.disks;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.barrelorgandiscovery.recognition.math.EllipseParameters;
import org.barrelorgandiscovery.recognition.math.MathVect;

/**
 * disk image tools
 * 
 * @author pfreydiere
 * 
 */
public class DiskImageTools {

	/**
	 * create a corrected image of the disk
	 * 
	 * @param source
	 *            the image source
	 * @param center
	 *            the center of the disk
	 * @param cp
	 *            the parameters of the disk perimeters (ellipse for more
	 *            precision)
	 * @param startAngle
	 *            the angle of the beginning of the disk
	 * @return the corrected image
	 * @throws Exception
	 */
	public static BufferedImage createCorrectedImage(BufferedImage source,
			Point2D.Double center, EllipseParameters cp, double startedAngle,
			int resultWidth, int resultHeight) throws Exception {

		assert source != null;
		assert center != null;
		assert cp != null;
		assert resultWidth > 0;
		assert resultHeight > 0;

		// compute the image size

		ColorModel dstCM = source.getColorModel();
		BufferedImage result = new BufferedImage(
				dstCM,
				dstCM.createCompatibleWritableRaster(resultWidth, resultHeight),
				dstCM.isAlphaPremultiplied(), null);

		for (int x = 0; x < result.getWidth(); x++) {
			// trigo 
			double angle =   (2.0 * Math.PI) / (1.0 * result.getWidth())
					* -x + startedAngle;

			MathVect posOnEllipse = new MathVect((cp.centre.x + cp.a
					* Math.cos(angle - cp.angle)), (cp.centre.y + cp.b
					* Math.sin(angle - cp.angle)));
			MathVect posCenter = new MathVect(center.x, center.y);

			for (int y = 0; y < result.getHeight(); y++) {
				MathVect interpolate = posOnEllipse.moins(posCenter)
						.scale(1.0 - ((1.0 * y) / result.getHeight())).rotate(cp.angle)
						.plus(posCenter);
				int ox = (int) interpolate.getX();
				int oy = (int) interpolate.getY();

				if (ox >= 0 && ox < source.getWidth() && oy >= 0
						&& oy < source.getHeight()) {
					try {
						int[] v = getRGB(source, ox, oy, 1, 1, null);
						setRGB(result, x, y, 1, 1, v);
					} catch (Exception ex) {
						System.out.println("pixel " + ox + "," + oy //$NON-NLS-1$ //$NON-NLS-2$
								+ " out of bounds"); //$NON-NLS-1$
					}
				}
			}
		}

		return result;
	}

	/**
	 * A convenience method for getting ARGB pixels from an image. This tries to
	 * avoid the performance penalty of BufferedImage.getRGB unmanaging the
	 * image.
	 * 
	 * @param image
	 *            a BufferedImage object
	 * @param x
	 *            the left edge of the pixel block
	 * @param y
	 *            the right edge of the pixel block
	 * @param width
	 *            the width of the pixel arry
	 * @param height
	 *            the height of the pixel arry
	 * @param pixels
	 *            the array to hold the returned pixels. May be null.
	 * @return the pixels
	 * @see #setRGB
	 */
	private static int[] getRGB(BufferedImage image, int x, int y, int width,
			int height, int[] pixels) {
		int type = image.getType();
		if (type == BufferedImage.TYPE_INT_ARGB
				|| type == BufferedImage.TYPE_INT_RGB)
			return (int[]) image.getRaster().getDataElements(x, y, width,
					height, pixels);
		return image.getRGB(x, y, width, height, pixels, 0, width);
	}

	/**
	 * A convenience method for setting ARGB pixels in an image. This tries to
	 * avoid the performance penalty of BufferedImage.setRGB unmanaging the
	 * image.
	 * 
	 * @param image
	 *            a BufferedImage object
	 * @param x
	 *            the left edge of the pixel block
	 * @param y
	 *            the right edge of the pixel block
	 * @param width
	 *            the width of the pixel arry
	 * @param height
	 *            the height of the pixel arry
	 * @param pixels
	 *            the array of pixels to set
	 * @see #getRGB
	 */
	private static void setRGB(BufferedImage image, int x, int y, int width,
			int height, int[] pixels) {
		int type = image.getType();
		if (type == BufferedImage.TYPE_INT_ARGB
				|| type == BufferedImage.TYPE_INT_RGB)
			image.getRaster().setDataElements(x, y, width, height, pixels);
		else
			image.setRGB(x, y, width, height, pixels, 0, width);
	}

}
