package org.barrelorgandiscovery.images.books.tools;

import java.io.File;

public interface IFileBasedTiledImage extends ITiledImage {

	/**
	 * file image path associated to its index
	 * 
	 * @param i
	 * @return
	 */
	File getImagePath(int i);
	
}
