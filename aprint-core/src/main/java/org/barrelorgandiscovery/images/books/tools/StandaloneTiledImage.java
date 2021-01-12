package org.barrelorgandiscovery.images.books.tools;

import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;

import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;

/**
 * wrapper for standlone image into tiled images
 * 
 * @author pfreydiere
 *
 */
public class StandaloneTiledImage implements ITiledImage, IFileBasedTiledImage, IFamilyImageSeeker {

	BufferedImage image;
	
	
	public StandaloneTiledImage(BufferedImage image) {
		this.image = image;
	}

	@Override
	public int getWidth() {
		return image.getWidth();
	}

	@Override
	public int getHeight() {
		return image.getHeight();
	}

	@Override
	public int getImageCount() {
		return 1;
	}

	@Override
	public File getImagePath(int i) {
		return null;
	}

	public BufferedImage getImage() {
		return this.image;
	}

	@Override
	public Double subTileDimension(int index) {
		return new Double(0, 0, image.getWidth(), image.getHeight());
	}

	@Override
	public int[] subTiles(Double viewport) {
		return new int[] { 0 };
	}

	@Override
	public BufferedImage loadImage(int imageNumber) throws Exception {
		if (imageNumber == 0) 
		{
			return getImage();
		}
		return null;
	}

}
