package org.barrelorgandiscovery.images.books.tools;

import java.awt.geom.Rectangle2D;
import java.io.File;

/**
 * interface for getting tiled images
 * 
 * @author pfreydiere
 *
 */
public interface ITiledImage {

	/**
	 * get image width
	 * 
	 * @return
	 */
	int getWidth();
	
	/**
	 * get a single tile width
	 * @return
	 */
	int getTileWidth();

	/**
	 * get single image height
	 * 
	 * @return
	 */
	int getHeight();

	/**
	 * number of images
	 * 
	 * @return
	 */
	int getImageCount();

	/**
	 * grab the tile extend
	 * 
	 * @param index
	 * @return
	 */
	Rectangle2D.Double subTileDimension(int index);

	/**
	 * return the tiles that intersect the viewport
	 *
	 * @param viewport
	 * @return
	 */
	int[] subTiles(Rectangle2D.Double viewport);
}