package org.barrelorgandiscovery.recognition.gui.disks;

import java.awt.BorderLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.barrelorgandiscovery.math.MathVect;
import org.barrelorgandiscovery.recognition.math.EllipseParameters;

/**
 * disk image tools
 * 
 * @author pfreydiere
 * 
 */
public class DiskImageTools {

	/**
	 * interpolate between 2 ellipses
	 * 
	 * @param exterior
	 * @param interior
	 * @param angle
	 * @param ratio    0 it is on the inner ellipse, 1 on the outer
	 * @return
	 */
	public static MathVect interpolateBetween2Ellipse(EllipseParameters exterior, EllipseParameters interior,
			double angle, double ratio) {

		Double extPoint = exterior.computePointOnEllipse(angle, 1.0);
		Double intPoint = interior.computePointOnEllipse(angle, 1.0);

		// ratio inbetween
		MathVect v = new MathVect(extPoint.x - intPoint.x, extPoint.y - intPoint.y);
		
		double factor = 1/v.norme();
		
		return v.scale(ratio).plus(new MathVect(intPoint.x, intPoint.y));
	}

	/**
	 * create a corrected image of the disk
	 * 
	 * @param source     the image source
	 * @param center     the center of the disk
	 * @param cp         the parameters of the disk perimeters (ellipse for more
	 *                   precision)
	 * @param startAngle the angle of the beginning of the disk
	 * @return the corrected image
	 * @throws Exception
	 */
	public static BufferedImage createCorrectedImage(BufferedImage source, EllipseParameters inner,
			EllipseParameters outer, double startedAngle, int resultWidth, int resultHeight,
			int heightOfTheInnerEllipse) throws Exception {

		assert source != null;

		Point2D.Double center = inner.centre;

		assert center != null;

		assert inner != null;
		assert outer != null;

		assert resultWidth > 0;
		assert resultHeight > 0;

		// compute the image size

		ColorModel dstCM = source.getColorModel();
		BufferedImage result = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(resultWidth, resultHeight),
				dstCM.isAlphaPremultiplied(), null);

		for (int x = 0; x < result.getWidth(); x++) {
			// trigo
			double angleFromImage = (2.0 * Math.PI) / (1.0 * result.getWidth()) * -x + startedAngle;

			for (int y = 0; y < heightOfTheInnerEllipse; y++) {

				MathVect vect = interpolateBetween2Ellipse(outer, inner, angleFromImage ,
						(1.0d - 1.0d * y / heightOfTheInnerEllipse));

				int ox = (int) vect.getX();
				int oy = (int) vect.getY();

				if (ox >= 0 && ox < source.getWidth() && oy >= 0 && oy < source.getHeight()) {
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
	 * create a corrected image of the disk
	 * 
	 * @param source     the image source
	 * @param center     the center of the disk
	 * @param cp         the parameters of the disk perimeters (ellipse for more
	 *                   precision)
	 * @param startAngle the angle of the beginning of the disk
	 * @return the corrected image
	 * @throws Exception
	 */
	public static BufferedImage createCorrectedImage(BufferedImage source, Point2D.Double center, EllipseParameters cp,
			double startedAngle, int resultWidth, int resultHeight) throws Exception {

		assert source != null;
		assert center != null;
		assert cp != null;
		assert resultWidth > 0;
		assert resultHeight > 0;

		// compute the image size

		ColorModel dstCM = source.getColorModel();
		BufferedImage result = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(resultWidth, resultHeight),
				dstCM.isAlphaPremultiplied(), null);

		for (int x = 0; x < result.getWidth(); x++) {
			// trigo
			double angle = (2.0 * Math.PI) / (1.0 * result.getWidth()) * -x + startedAngle;

			MathVect posOnEllipse = new MathVect((cp.centre.x + cp.a * Math.cos(angle - cp.angle)),
					(cp.centre.y + cp.b * Math.sin(angle - cp.angle)));
			MathVect posCenter = new MathVect(center.x, center.y);

			for (int y = 0; y < result.getHeight(); y++) {
				MathVect interpolate = posOnEllipse.moins(posCenter).scale(1.0 - ((1.0 * y) / result.getHeight()))
						.rotate(cp.angle).plus(posCenter);
				int ox = (int) interpolate.getX();
				int oy = (int) interpolate.getY();

				if (ox >= 0 && ox < source.getWidth() && oy >= 0 && oy < source.getHeight()) {
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
	 * avoid the performance penalty of BufferedImage.getRGB unmanaging the image.
	 * 
	 * @param image  a BufferedImage object
	 * @param x      the left edge of the pixel block
	 * @param y      the right edge of the pixel block
	 * @param width  the width of the pixel arry
	 * @param height the height of the pixel arry
	 * @param pixels the array to hold the returned pixels. May be null.
	 * @return the pixels
	 * @see #setRGB
	 */
	private static int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
		int type = image.getType();
		if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
			return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
		return image.getRGB(x, y, width, height, pixels, 0, width);
	}

	/**
	 * A convenience method for setting ARGB pixels in an image. This tries to avoid
	 * the performance penalty of BufferedImage.setRGB unmanaging the image.
	 * 
	 * @param image  a BufferedImage object
	 * @param x      the left edge of the pixel block
	 * @param y      the right edge of the pixel block
	 * @param width  the width of the pixel arry
	 * @param height the height of the pixel arry
	 * @param pixels the array of pixels to set
	 * @see #getRGB
	 */
	private static void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
		int type = image.getType();
		if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
			image.getRaster().setDataElements(x, y, width, height, pixels);
		else
			image.setRGB(x, y, width, height, pixels, 0, width);
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// test method for this library

	// test method
	public static void main(String[] args) throws Exception {
		// squared image
		BufferedImage bi = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < bi.getWidth(); i++) {
			for (int j = 0; j < bi.getHeight(); j++) {
				int c = ((i / 10) + (j / 10)) % 2;

				setRGB(bi, i, j, 1, 1, new int[] { 255 * c, 255 * c, 255 * c });
			}
		}

		EllipseParameters inner = new EllipseParameters();
		inner.a = 20;
		inner.b = 20;
		inner.centre = new Point2D.Double(300, 280);
		inner.angle = 0;

		EllipseParameters outer = new EllipseParameters();
		outer.a = 200;
		outer.b = 300;
		outer.centre = new Point2D.Double(300, 300);
		outer.angle = 0;

		BufferedImage createCorrectedImage = createCorrectedImage(bi, inner, outer, 0.0, 600, 300, 100);

		display(bi);
		display(createCorrectedImage);

	}

	/**
	 * display a simple image
	 * 
	 * @param bi the bufferedimage
	 */
	private static void display(BufferedImage bi) {
		JFrame jframe = new JFrame();
		jframe.setSize(600, 600);
		jframe.getContentPane().setLayout(new BorderLayout());
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(bi));
		jframe.getContentPane().add(label, BorderLayout.CENTER);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true);
	}

}
