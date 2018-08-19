package org.barrelorgandiscovery.virtualbook.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.InputStream;

/**
 * musicbox rendering
 *
 * @author pfreydiere
 */
public class MusicBoxRendering extends VirtualBookRendering {

  public MusicBoxRendering() {}

  /*
   * (non-Javadoc)
   * @see org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering#getBackgroundImage()
   */
  @Override
  public InputStream getBackgroundImage() {
    return getClass().getResourceAsStream("blankbg.jpg");
  }

  /*
   * (non-Javadoc)
   * @see org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering#getDefaultBookColor()
   */
  @Override
  public Color getDefaultBookColor() {
    return Color.WHITE;
  }

 /*
  * (non-Javadoc)
  * @see org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering#renderHole(java.awt.Graphics2D, int, int, int, int, boolean)
  */
  public void renderHole(Graphics2D g2d, int x, int y, int width, int height, boolean isSelected) {

    if (!isSelected) {
      g2d.setColor(Color.BLACK);
    } else {
      g2d.setColor(Color.red);
    }

    double continueRatio = 0.3; // height * 0.3

    if (width < height * 2) {
    	  g2d.fillOval(x, y, height, height);
      
    } else {

      int i = (int) ((1 + continueRatio) * height);
      
      g2d.fillOval(x, y, height, height);
      // g2d.fillRoundRect(x, y, (int) (1.5 * height), height, height, height);

      while (i <= width) {

        if ((width - i) < (2 * height)) {

          // en to draw ...

          int lwidth = (width - i);
          if (lwidth < height) {
            lwidth = height;
            i = (width - height);
          }

          g2d.fillOval(i + x, y,  height, height);

          break;

        } else {
            g2d.fillOval(i + x, y,  height, height);

        }

        i += (int) (height * (1.0 + continueRatio));
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering#getName()
   */
  @Override
  public String getName() {
    return "musicbox";
  }
}
