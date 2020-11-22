package org.barrelorgandiscovery.images.books.tools;

import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;

import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;
import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.tools.ImageTools;

public class BookImageRecognitionTiledImage implements ITiledImage, IFileFamilyTiledImage, IFamilyImageSeeker {

	private ZipBookImage zipBookImage;

	private File outputRecognitionProject;

	public BookImageRecognitionTiledImage(ZipBookImage bookImage) {
		assert bookImage != null;
		this.zipBookImage = bookImage;

		File originFile = bookImage.getBookImageFile();
		File od = new File(originFile.getParentFile(), originFile.getName() + RecognitionTiledImage.TILED_EXTENSION);
		od.mkdirs();

		setTiledImageDirectory(od);

	}

	protected void setTiledImageDirectory(File tileImageDirectory) {
		assert tileImageDirectory != null;
		assert tileImageDirectory.exists();
		assert tileImageDirectory.isDirectory();

		this.outputRecognitionProject = tileImageDirectory;
	}

	@Override
	public BufferedImage loadImage(int imageNumber) throws Exception {
		return loadImage(imageNumber, currentImageFamilyDisplay);
	}

	@Override
	public BufferedImage loadImage(int index, String suffix) throws Exception {
		if (suffix == null) {
			return zipBookImage.loadImage(index);
		}

		File f = constructImagePath(index, suffix);
		if (!f.exists()) {
			return null;
		}

		return ImageTools.loadImage(f);
	}

	@Override
	public File constructImagePath(int index, String suffix) {
		if (currentImageFamilyDisplay == null) {
			throw new RuntimeException("null family display has no filepath");
		}

		return new File(outputRecognitionProject, "" + index + (suffix == null ? "" : "_" + suffix) + ".jpg");
	}

	@Override
	public File getImagePath(int i) {
		if (currentImageFamilyDisplay == null) {
			throw new RuntimeException("not image path for null family display");
		}

		return constructImagePath(i, currentImageFamilyDisplay);
	}

	private String currentImageFamilyDisplay = null;

	@Override
	public void setCurrentImageFamilyDisplay(String imagedisplay) {
		this.currentImageFamilyDisplay = imagedisplay;
	}

	@Override
	public String getCurrentImageFamilyDisplay() {
		return this.currentImageFamilyDisplay;
	}

	@Override
	public int getWidth() {
		return zipBookImage.getWidth();
	}

	@Override
	public int getHeight() {
		return zipBookImage.getHeight();
	}

	@Override
	public int getImageCount() {
		return zipBookImage.getImageCount();

	}

	@Override
	public Double subTileDimension(int index) {
		return zipBookImage.subTileDimension(index);
	}

	@Override
	public int[] subTiles(Double viewport) {
		return zipBookImage.subTiles(viewport);
	}

}
