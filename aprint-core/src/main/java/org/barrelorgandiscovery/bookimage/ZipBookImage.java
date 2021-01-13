package org.barrelorgandiscovery.bookimage;

import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.Disposable;

/**
 * this file is a seekable image file family,
 * 
 * @author pfreydiere
 *
 */
public class ZipBookImage extends BookImage implements Disposable {

	public static final String IMAGE_PREFIX = "image_";

	private static Logger logger = Logger.getLogger(ZipBookImage.class);

	private int imageCount;

	private ZipFile zipFile;
	private File zipFilePath;

	public ZipBookImage(File zipFile) throws Exception {
		assert zipFile != null;
		this.zipFilePath = zipFile;
		this.zipFile = new ZipFile(zipFile);
		Pattern p = Pattern.compile("image\\_([0-9]{4,4})\\.jpg");
		// count image number
		Enumeration<? extends ZipEntry> e = this.zipFile.entries();
		int max = -1;
		for (ZipEntry z = e.nextElement(); e.hasMoreElements(); z = e.nextElement()) {
			Matcher m = p.matcher(z.getName());
			if (m.matches()) {
				int c = Integer.parseInt(m.group(1));
				max = Math.max(max, c);
			}
		}
		imageCount = max + 1;
	}

	@Override
	public int getImageCount() {
		return imageCount;
	}

	public static String constructEntryName(int i) {
		return IMAGE_PREFIX + String.format("%04d", i) + ".jpg";
	}

	private LRUCache<Integer, WeakReference<BufferedImage>> lruCache = new LRUCache<>(10);

	@Override
	public BufferedImage loadImage(int imageNumber) throws Exception {

		WeakReference<BufferedImage> ref = lruCache.get((Integer) imageNumber);
		if (ref != null) {
			BufferedImage r = ref.get();
			if (r != null) {
				return r;
			}
		}
		String entryName = constructEntryName(imageNumber);
		ZipEntry zipEntry = zipFile.getEntry(entryName);
		if (zipEntry == null) {
			logger.debug("zip entry not found");
			return null;
		}
		InputStream istream = zipFile.getInputStream(zipEntry);
		if (istream == null) {
			return null;
		}
		BufferedImage b = ImageIO.read(istream);
		lruCache.set((Integer) imageNumber, new WeakReference<BufferedImage>(b));
		return b;
	}

	int width = -1;
	int height = -1;

	private void populateWidthAndHeightFromFirst() {

		if (width == -1 || height == -1) {
			try {
				BufferedImage loadImage = loadImage(0);
				this.width = loadImage.getWidth();
				this.height = loadImage.getHeight();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}

	}

	@Override
	public int getWidth() {
		populateWidthAndHeightFromFirst();
		return this.width;
	}

	@Override
	public int getTileWidth() {
		populateWidthAndHeightFromFirst();
		return this.width;
	}
	
	@Override
	public int getHeight() {
		populateWidthAndHeightFromFirst();
		return this.height;
	}

	@Override
	public Double subTileDimension(int index) {
		return new Double(index * getTileWidth(), 0, (index + 1) * getTileWidth(), getHeight());
	}

	@Override
	public int[] subTiles(Double viewport) {
		int startx = (int) Math.floor(viewport.x / (getTileWidth() * 1.0));
		int endx = (int) Math.ceil((viewport.x + viewport.width) / (getTileWidth() * 1.0));
		int[] result = new int[endx - startx + 1];
		for (int i = startx; i <= endx; i++) {
			result[i - startx] = i;
		}
		return result;
	}

	public File getBookImageFile() {
		return this.zipFilePath;
	}

	@Override
	public void dispose() {
		lruCache.clear();
	}

}
