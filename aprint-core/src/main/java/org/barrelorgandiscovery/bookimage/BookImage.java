package org.barrelorgandiscovery.bookimage;

import java.awt.image.BufferedImage;

import org.barrelorgandiscovery.images.books.tools.ITiledImage;
import org.barrelorgandiscovery.scale.Scale;

/**
 * book image reading implementation
 * 
 * @author pfreydiere
 * 
 */
public abstract class BookImage implements IFamilyImageSeeker, ITiledImage {

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
