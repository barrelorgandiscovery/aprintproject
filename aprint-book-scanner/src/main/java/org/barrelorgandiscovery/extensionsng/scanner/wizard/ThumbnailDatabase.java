package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import org.barrelorgandiscovery.tools.ImageTools;

/**
 * class managing a thumbnail creation for folder having images.
 * handle a resample level in the view, with folder storage
 * 
 * @author pfreydiere
 *
 */
public class ThumbnailDatabase {

	private int width;
	private int height;

	private static final String THUMBNAILFOLDER = ".thumbails";

	private File folder;

	public ThumbnailDatabase(File folder, int width, int height) throws Exception {
		this.folder = folder;
		assert folder != null;
		assert folder.isDirectory();

		this.width = width;
		this.height = height;
	}

	private File constructThumbnailFile(File f) {
		String name = "" + width + "x" + height + "_" + f.getName();
		File tf = new File(folder, THUMBNAILFOLDER);
		return new File(tf, name);
	}
	
	public boolean thumbnailExists(File f) throws Exception {
		File tf = constructThumbnailFile(f);

		return tf.exists();
	}

	public BufferedImage getOrCreate(File f) throws Exception {

		File tf = constructThumbnailFile(f);
		if (!tf.getParentFile().exists()) {
			tf.getParentFile().mkdirs();
		}

		if (tf.exists()) {
			return ImageTools.loadImage(tf.toURL());
		}

		BufferedImage dest = ImageTools.loadImageAndCrop(f, width, height);
		FileOutputStream fos = new FileOutputStream(tf);
		try {
			ImageIO.write(dest, "JPEG", fos);
		} finally {
			fos.close();
		}
		return dest;
	}
}
