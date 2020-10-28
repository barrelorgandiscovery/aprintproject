package org.barrelorgandiscovery.recognition.gui.books.tools;

import java.io.File;

import org.barrelorgandiscovery.recognition.gui.books.tools.TiledImage;

/**
 * this tiled image create an associated output directory
 * 
 * @author pfreydiere
 *
 */
public class RecognitionTiledImage extends TiledImage {

	private String currentImageFamilyDisplay;

	public RecognitionTiledImage(RecognitionTiledImage ti) throws Exception {
		this(ti.getImagePath(), ti.getHeight());
	}
	

	public RecognitionTiledImage(File imagePath) throws Exception {
		super(imagePath, null);

		File od = new File(imagePath.getParentFile(), imagePath.getName() + ".tiled");
		od.mkdirs();

		setTiledImageDirectory(od);
	}

	
	public RecognitionTiledImage(File imagePath, int maxHeight) throws Exception {
		super(imagePath, null, maxHeight);

		File od = new File(imagePath.getParentFile(), imagePath.getName() + ".tiled");
		od.mkdirs();

		setTiledImageDirectory(od);
	}

	public File constructImagePath(int index, String suffix) {
		return super.constructImagePath(index, suffix);
	}

	@Override
	public File getImagePath(int i) {
		if (currentImageFamilyDisplay == null)
			return super.getImagePath(i);

		return constructImagePath(i, currentImageFamilyDisplay);
	}

	public void setCurrentImageFamilyDisplay(String imagedisplay) {
		this.currentImageFamilyDisplay = imagedisplay;
	}
	
	public String getCurrentImageFamilyDisplay() {
		return currentImageFamilyDisplay;
	}
	
}
