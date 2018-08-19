package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.barrelorgandiscovery.recognition.math.MathLine;

/**
 * handle control point creation and a circle creation and drawing
 * 
 * @author pfreydiere
 * 
 */
public class JCircleLayerByTreePoints extends JShapeLayer<Rectangle2D.Double> {

	private Point2D.Double center;
	private double radius = Double.NaN;

	public JCircleLayerByTreePoints() {
	}

	private Point2D.Double center(Rectangle2D.Double r) {
		return new Point2D.Double(r.x + r.width / 2, r.y + r.height / 2);
	}

	@Override
	protected void fireLayerContentChanged() {

		List<java.awt.geom.Rectangle2D.Double> ug = getGraphics();
		if (ug.size() >= 3) {
			java.awt.geom.Point2D.Double pt1 = center(ug.get(0));
			java.awt.geom.Point2D.Double pt2 = center(ug.get(1));
			java.awt.geom.Point2D.Double pt3 = center(ug.get(2));

			MathLine mathLineV1 = new MathLine(pt1, pt2);

			MathLine o1 = new MathLine(mathLineV1.center(), mathLineV1
					.getVecteur().orthogonal());

			MathLine mathLineV2 = new MathLine(pt2, pt3);

			MathLine o2 = new MathLine(mathLineV2.center(), mathLineV2
					.getVecteur().orthogonal());

			java.awt.geom.Point2D.Double c = o1.intersect(o2);

			this.center = c;
			this.radius = pt1.distance(c);

		} else {
			this.center = null;
			this.radius = Double.NaN;
		}

		super.fireLayerContentChanged();
	}

	@Override
	public void drawLayer(Graphics2D g2d) {
		super.drawLayer(g2d);

		if (!Double.isNaN(radius) && center != null) {
			Ellipse2D.Double e = new Ellipse2D.Double();
			e.x = center.getX() - radius;
			e.y = center.getY() - radius;
			e.width = 2 * radius;
			e.height = e.width;
			g2d.draw(e);
		}
	}

	@Override
	public void add(java.awt.geom.Rectangle2D.Double shape) {
		// force only 3 points
		List<java.awt.geom.Rectangle2D.Double> graphics = getGraphics();
		if (graphics.size() < 3)
			super.add(shape);
	}

	public Point2D.Double getCenter() {
		return center;
	}

	public double getRadius() {
		return radius;
	}

}
