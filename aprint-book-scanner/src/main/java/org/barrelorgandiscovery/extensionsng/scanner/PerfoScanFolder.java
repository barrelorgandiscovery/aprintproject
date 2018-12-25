package org.barrelorgandiscovery.extensionsng.scanner;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.barrelorgandiscovery.tools.ImageTools;

/**
 * this is a utility class for folder in wich the images are taken
 * 
 * @author pfreydiere
 *
 */
public class PerfoScanFolder {

	private static final int MAX_IMAGE_IN_FOLDER = 500;
	private File folder;
	private int count = 0;

	/**
	 * Constructor
	 * 
	 * @param folder
	 */
	public PerfoScanFolder(File folder) {
		assert folder != null;
		assert folder.exists();
		assert folder.isDirectory();
		this.folder = folder;
		this.count = getImageCount();
	}

	/**
	 * count the number of images in folder assuming there are continuous
	 * 
	 * @return
	 */
	public int getImageCount() {
		for (int i = MAX_IMAGE_IN_FOLDER; i > 0; i--) {
			if (constructImageFile(i).exists()) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * delete all images
	 */
	public void deleteAllImages() {
		for (int i = MAX_IMAGE_IN_FOLDER; i > 0; i--) {
			File consideredImages = constructImageFile(i);
			if (consideredImages.exists()) {
				consideredImages.delete();
			}
		}
	}

	/**
	 * Store a new Image in the store, return the count number
	 * 
	 * @param image
	 * @return
	 * @throws Exception
	 */
	public int addNewImage(Image image) throws Exception {
		int i = this.count++;
		BufferedImage bi = null;
		if (image instanceof BufferedImage) {
			bi = (BufferedImage) image;
		} else {
			bi = ImageTools.loadImage(image);
		}
		ImageIO.write(bi, "JPEG", constructImageFile(i));
		return i;
	}

	/**
	 * construct the image name from index
	 * 
	 * @param sequence
	 * @return
	 */
	public String constructImageName(int sequence) {
		return "scan_image_" + sequence;
	}

	/**
	 * construct the image file object
	 * 
	 * @param sequence
	 * @return
	 */
	public File constructImageFile(int sequence) {
		return new File(folder, constructImageName(sequence) + ".jpg");
	}

	/**
	 * load the image from index
	 * 
	 * @param sequence
	 * @return the full image
	 * @throws Exception
	 */
	public BufferedImage loadImage(int sequence) throws Exception {
		return ImageTools.loadImage(constructImageFile(sequence).toURL());
	}

}
