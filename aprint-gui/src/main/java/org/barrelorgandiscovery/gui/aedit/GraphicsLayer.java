package org.barrelorgandiscovery.gui.aedit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Graphics layer for displaying shapes on the virtualbook this class is a
 * container for graphics. the color / drawings options are the same for all the
 * elements of the collection.
 * 
 * Shapes are under the book coordinate system (mm).
 * 
 * 
 * @author pfreydiere
 * 
 */
public class GraphicsLayer extends ArrayList<Shape> implements
		VirtualBookComponentLayer, VirtualBookComponentLayerName {

	private static Logger logger = Logger.getLogger(GraphicsLayer.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -999191083230120071L;

	private Paint paint = null;
	private Stroke stroke = new BasicStroke(1.0f);
	private Color color = Color.pink;

	/**
	 * Set paint for the display
	 * 
	 * @param paint
	 */
	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	/**
	 * get the paint for the display
	 * 
	 * @return
	 */
	public Paint getPaint() {
		return paint;
	}

	/**
	 * define the stroke for painting
	 * 
	 * @param stroke
	 */
	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	/**
	 * get the stroke for the paint
	 * 
	 * @return
	 */
	public Stroke getStroke() {
		return stroke;
	}

	/**
	 * set the paint color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * get the paint color
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	private String displayName;

	public GraphicsLayer(String displayname) throws Exception {
		if (displayname == null)
			throw new Exception("layer must have a name");

		this.displayName = displayname;
	}

	public String getDisplayName() {
		return displayName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer#draw(java
	 * .awt.Graphics, org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent)
	 */
	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference) {

		if (!visible)
			return;

		Graphics2D g2d = (Graphics2D) g;
		Rectangle clipBounds = new Rectangle(g.getClipBounds());

		Stroke oldstroke = g2d.getStroke();
		try {

			if (stroke != null)
				g2d.setStroke(stroke);

			Color oldColor = g2d.getColor();
			try {

				g2d.setColor(color);
				g2d.setPaintMode();

				Paint oldpaint = g2d.getPaint();
				try {

					g2d.setPaint(paint);

					AffineTransform old = g2d.getTransform();
					try {

						AffineTransform t = AffineTransform
								.getTranslateInstance(
										-jbookcomponentreference.getXoffset()
												+ jbookcomponentreference
														.getMargin(),
										-jbookcomponentreference.getYoffset()
												+ jbookcomponentreference
														.getMargin());

						double f = 10 / jbookcomponentreference.pixelToMm(10);
						AffineTransform scale = AffineTransform
								.getScaleInstance(f, f);
						scale.concatenate(t);

						AffineTransform clone = (AffineTransform) old.clone();

						clone.concatenate(scale);

						g2d.setTransform(clone);

						// revert transform for having the clipbounds in the
						// current
						Rectangle2D displayedbbox = null;
						try {
							displayedbbox = scale.createInverse()
									.createTransformedShape(clipBounds)
									.getBounds2D();
						} catch (Exception ex) {
							logger.error("error in compute inverse transform :"
									+ ex.getMessage(), ex);
						}

						for (Iterator<Shape> iterator = this.iterator(); iterator
								.hasNext();) {

							Shape s = (Shape) iterator.next();

							// as we have transforms, this tests is not correct,
							//
							try {
								if (s == null)
									continue;
								
								// intersects method doesn't work properly
								// we had reimplemented the bbox intersection
								
								Rectangle2D shapebounds2d = s.getBounds2D();
								if (shapebounds2d.getMinX() > displayedbbox
										.getMaxX())
									continue;

								if (shapebounds2d.getMaxX() < displayedbbox
										.getMinX())
									continue;

								if (shapebounds2d.getMinY() > displayedbbox
										.getMaxY())
									continue;

								if (shapebounds2d.getMaxY() < displayedbbox
										.getMinY())
									continue;

								
								
								// if (displayedbbox != null
								// && !displayedbbox.intersects(s
								// .getBounds2D()))
								// continue;

								g2d.draw(s);
							} catch (Exception ex) {
								logger.debug("fail to draw on shape :" + s
										+ " -> " + ex.getMessage(), ex);
							}
						}

					} finally {
						g2d.setTransform(old);
					}
				} finally {
					g2d.setPaint(oldpaint);
				}

			} finally {
				g2d.setColor(oldColor);
			}
		} finally {
			g2d.setStroke(oldstroke);
		}

	}

	private boolean visible = true;

	public void setVisible(boolean visible) {
		this.visible = visible;

	}

	public boolean isVisible() {
		return this.visible;
	}

}
