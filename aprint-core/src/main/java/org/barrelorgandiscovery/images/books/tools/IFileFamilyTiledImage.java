package org.barrelorgandiscovery.images.books.tools;

import java.awt.image.BufferedImage;
import java.io.File;

public interface IFileFamilyTiledImage extends ITiledImage {

	File constructImagePath(int index, String suffix);
	
	BufferedImage loadImage(int index, String suffix) throws Exception;

	File getImagePath(int i);

	void setCurrentImageFamilyDisplay(String imagedisplay);

	String getCurrentImageFamilyDisplay();
	

}