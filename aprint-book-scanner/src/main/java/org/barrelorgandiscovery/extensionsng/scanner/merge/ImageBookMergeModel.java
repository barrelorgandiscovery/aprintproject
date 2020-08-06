package org.barrelorgandiscovery.extensionsng.scanner.merge;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import org.barrelorgandiscovery.math.MathVect;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

/**
 * Model
 * @author pfreydiere
 *
 */
public class ImageBookMergeModel implements Serializable{

  public ImageBookMergeModel() {}

  Point2D.Double origin;
  Point2D.Double pointforAngleAndImageWidth;
  Point2D.Double pointforBookWidth;
  
  double overlappDistance;

  public AffineTransform computeTransform(double factor) {

    double p2x = pointforAngleAndImageWidth.getX();
    double p1x = origin.getX();
    double p2y = pointforAngleAndImageWidth.getY();
    double p1y = origin.getY();
    MathVect v = new MathVect(p2x - p1x, p2y - p1y);

    AffineTransform t = AffineTransform.getTranslateInstance(-p1x, -p1y);

    t.preConcatenate(AffineTransform.getScaleInstance(factor, factor));

    t.preConcatenate(AffineTransform.getRotateInstance(-v.angleOrigine()));

    t.preConcatenate(AffineTransform.getScaleInstance(1.0, -1.0));

    return t;
  }

  public MathVect getBookWidthVector() {
    return new MathVect(pointforBookWidth.getX() - origin.getX(), pointforBookWidth.getY() - origin.getY());
  }
  
  public MathVect getAngleAndImageWidthVector() {
	  return new MathVect(pointforAngleAndImageWidth.getX() - origin.getX(), pointforAngleAndImageWidth.getY() - origin.getY());
  }

  public BufferedImage createSlice(BufferedImage current, int outImageWidth, int outImageHeight) {

    MathVect b = getBookWidthVector();
    double factor = 1.0 / b.norme() * outImageHeight;

    AffineTransform t = computeTransform(factor);

    BufferedImage newImage =
        new BufferedImage(outImageWidth, outImageHeight, BufferedImage.TYPE_INT_ARGB);

    {
      Graphics2D g = (Graphics2D) newImage.getGraphics();
      try {
        AffineTransform timage = g.getTransform();
        timage.concatenate(t);
        RenderingHints renderingHints = g.getRenderingHints();

        renderingHints.put(
            RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHints(renderingHints);

        g.setTransform(timage);
        g.drawImage(current, 0, 0, null);
      } finally {
        g.dispose();
      }
    }
    return newImage;
  }
  
  
  ////////////////////////////////////////////////////////////////////
  // load / save properties
  
  public void loadFrom(IPrefsStorage storage) {
	origin = new Point2D.Double(storage.getDoubleProperty("originx", 0),
			storage.getDoubleProperty("originy", 0));
	
	pointforAngleAndImageWidth = new Point2D.Double(storage.getDoubleProperty("pointanglex", 0),
			storage.getDoubleProperty("pointangley", 0));
	
	pointforBookWidth = new Point2D.Double(storage.getDoubleProperty("pointwidthx", 0),
			storage.getDoubleProperty("pointwidthy", 0));
	
	overlappDistance = storage.getDoubleProperty("overlapdistance", 100);
	
  }
  
  public void saveTo(IPrefsStorage storage) {
	  
	  storage.setDoubleProperty("originx", origin.x);
	  storage.setDoubleProperty("originy", origin.y);
	  
	  storage.setDoubleProperty("pointanglex", pointforAngleAndImageWidth.x);
	  storage.setDoubleProperty("pointangley", pointforAngleAndImageWidth.y);
	  
	  storage.setDoubleProperty("pointwidthx", pointforBookWidth.x);
	  storage.setDoubleProperty("pointwidthy", pointforBookWidth.y);
	  
	  storage.setDoubleProperty("overlapdistance", overlappDistance);

	  storage.save();
  }
  
}
