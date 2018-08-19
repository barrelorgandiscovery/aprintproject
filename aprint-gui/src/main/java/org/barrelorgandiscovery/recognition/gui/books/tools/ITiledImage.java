package org.barrelorgandiscovery.recognition.gui.books.tools;

import java.awt.geom.Rectangle2D;
import java.io.File;

public interface ITiledImage {

  int getWidth();

  int getHeight();

  /**
   * number of images
   * @return
   */
  int getImageCount();

  /**
   * file image path associated to its index
   * @param i
   * @return
   */
  File getImagePath(int i);

  /**
   * grab the tile extend
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