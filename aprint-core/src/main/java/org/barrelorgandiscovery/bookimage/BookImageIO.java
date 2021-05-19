package org.barrelorgandiscovery.bookimage;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.ProgressIndicator;
import org.barrelorgandiscovery.tools.ImageTools;

public class BookImageIO {

	private static final Logger logger = Logger.getLogger(BookImageIO.class);

	/**
	 * Create a book image
	 * 
	 * @param bufferedImage
	 * @param rotate
	 * @throws Exception
	 */
	public static void createBookImage(BufferedImage bufferedImage, File destinationBookImage, boolean rotate,
			ICancelTracker cancelTracker, ProgressIndicator progress) throws Exception {

		int height = bufferedImage.getHeight();
		if (rotate) {
			height = bufferedImage.getWidth();
		}

		FileOutputStream fos = new FileOutputStream(destinationBookImage);
		try {
			ZipOutputStream zipOutputStream = new ZipOutputStream(fos);
			try {
				boolean endreach = false;
				int currentimageindex = 0;

				// each image is a square of "height" dimensions

				while (!endreach) {
					logger.debug("writing image " + currentimageindex); //$NON-NLS-1$

					if (cancelTracker != null && cancelTracker.isCanceled()) {
						logger.info("save canceled by the user"); //$NON-NLS-1$
						return;
					}

					int initialPixelOfGeneratedImage = currentimageindex * height;

					int sx = currentimageindex * height;
					int sy = 0;

					if (rotate) {
						sx = 0;
						sy = currentimageindex * height;
					}

					BufferedImage sliceImage = new BufferedImage(height, height, BufferedImage.TYPE_INT_RGB);
					Graphics2D g2d = (Graphics2D) sliceImage.getGraphics();
					try {

						AffineTransform t = AffineTransform.getTranslateInstance(-sx, -sy);

						if (rotate) {
							
							t.preConcatenate(AffineTransform.getRotateInstance(-Math.PI / 2));
							t.preConcatenate(AffineTransform.getTranslateInstance(0, height));
						}
						g2d.drawImage(bufferedImage, t, null);
						// t.preConcatenate(AffineTransform.getTranslateInstance(-sx, -sy));

//						g2d.drawImage(bufferedImage, 0, 0, height-1, height -1, 
//								sx, sy, sx + height - 1, sy + height - 1, null);
//						

					} finally {
						g2d.dispose();
					}

					ZipEntry ze = new ZipEntry(ZipBookImage.constructEntryName(currentimageindex));
					zipOutputStream.putNextEntry(ze);

					ImageTools.saveJpeg(sliceImage, zipOutputStream);// $NON-NLS-1$

					// on openjdk, transparent jpeg are not supported
					// ImageIO.write(img, "JPEG", zipOutputStream); //$NON-NLS-1$

					zipOutputStream.flush();
					zipOutputStream.closeEntry();

					int imageCount = bufferedImage.getWidth() / bufferedImage.getHeight();
					if (rotate) {
						imageCount = bufferedImage.getHeight() / bufferedImage.getWidth();
					}
					try {
						if (progress != null) {
							progress.progress(1.0 * currentimageindex / imageCount, "Saving " + currentimageindex); //$NON-NLS-1$
						}
					} catch (Throwable t) {
						logger.error(t.getMessage(), t);
					}

					if (currentimageindex >= imageCount) {
						endreach = true;
					}
					currentimageindex++;
				}

			} finally {
				zipOutputStream.close();
			}

		} finally {
			fos.close();
		}
	}

}
