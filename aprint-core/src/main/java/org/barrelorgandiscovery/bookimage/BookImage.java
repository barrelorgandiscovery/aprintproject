package org.barrelorgandiscovery.bookimage;

import java.awt.image.BufferedImage;

import org.barrelorgandiscovery.images.books.tools.IFamilyImageSeekerTiledImage;
import org.barrelorgandiscovery.images.books.tools.ITiledImage;
import org.barrelorgandiscovery.scale.Scale;

/**
 * Book image reading abstract (or base) implementation.
 * for more informations about the bookimage, see documentation
 * 
 * @author pfreydiere
 * 
 */
public abstract class BookImage implements IFamilyImageSeekerTiledImage {

	public static final String BOOKIMAGE_EXTENSION_WITHOUT_DOT = "bookimage";
	public static final String BOOKIMAGE_EXTENSION = "." + BOOKIMAGE_EXTENSION_WITHOUT_DOT;
	
	private Scale virtualBookScale;

	BookImage() {

	}

	protected void init(Scale scale) {
		assert scale != null;
		this.virtualBookScale = scale;
	}

	public Scale getScale() {
		return virtualBookScale;
	}

	@Override
	public abstract int getImageCount();

	@Override
	public abstract BufferedImage loadImage(int imageNumber) throws Exception;

	

	
}
