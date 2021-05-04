package org.barrelorgandiscovery.images.books.tools;

import java.awt.image.BufferedImage;
import java.io.File;

import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * this tiled image create an associated output directory
 * 
 * @author pfreydiere
 *
 */
public class RecognitionTiledImage extends TiledImage implements IFileFamilyTiledImage, IFamilyImageSeeker,IFamilyImageSeekerTiledImage {

	private String currentImageFamilyDisplay;

	public static final String TILED_EXTENSION = ".tiled";

	/**
	 * copy constructor for creating a separate image family
	 * @param ti
	 * @throws Exception
	 */
	public RecognitionTiledImage(RecognitionTiledImage ti) throws Exception {
		this(ti.getImagePath(), ti.getHeight());
	}

	public RecognitionTiledImage(File imagePath) throws Exception {
		super(imagePath, null);

		File od = new File(imagePath.getParentFile(), imagePath.getName() + TILED_EXTENSION);
		od.mkdirs();

		setTiledImageDirectory(od);
	}

	public RecognitionTiledImage(File imagePath, int maxHeight) throws Exception {
		super(imagePath, null, maxHeight);

		File od = new File(imagePath.getParentFile(), imagePath.getName() + TILED_EXTENSION);
		od.mkdirs();

		setTiledImageDirectory(od);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.recognition.gui.books.tools.IFamilyTiledImage#
	 * constructImagePath(int, java.lang.String)
	 */
	@Override
	public File constructImagePath(int index, String suffix) {
		return super.constructImagePath(index, suffix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.recognition.gui.books.tools.IFamilyTiledImage#
	 * getImagePath(int)
	 */
	@Override
	public File getImagePath(int i) {
		if (currentImageFamilyDisplay == null) {
			return super.getImagePath(i);
		}

		return constructImagePath(i, currentImageFamilyDisplay);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.recognition.gui.books.tools.IFamilyTiledImage#
	 * setCurrentImageFamilyDisplay(java.lang.String)
	 */
	@Override
	public void setCurrentImageFamilyDisplay(String imagedisplay) {
		this.currentImageFamilyDisplay = imagedisplay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.recognition.gui.books.tools.IFamilyTiledImage#
	 * getCurrentImageFamilyDisplay()
	 */
	@Override
	public String getCurrentImageFamilyDisplay() {
		return currentImageFamilyDisplay;
	}

	/*
	 * (non-Javadoc)
	 * @see org.barrelorgandiscovery.bookimage.IFamilyImageSeeker#loadImage(int)
	 */
	@Override
	public BufferedImage loadImage(int imageNumber) throws Exception {
		return loadImage(imageNumber, currentImageFamilyDisplay);
	}

	/*
	 * (non-Javadoc)
	 * @see org.barrelorgandiscovery.images.books.tools.IFileFamilyTiledImage#loadImage(int, java.lang.String)
	 */
	@Override
	public BufferedImage loadImage(int index, String suffix) throws Exception {
		File f = constructImagePath(index, suffix);
		if (!f.exists()) {
			return null;
		}
		return ImageTools.loadImage(f);
	}

}
