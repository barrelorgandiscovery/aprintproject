package org.barrelorgandiscovery.recognition.gui.disks.steps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.awt.geom.Point2D;

import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JEllipticLayer;
import org.barrelorgandiscovery.recognition.math.EllipseParameters;
import org.barrelorgandiscovery.recognition.math.MathVect;
import org.barrelorgandiscovery.scale.Scale;

import Jama.Matrix;

/**
 * layer adding the display of the borders of tracks
 * 
 * @author pfreydiere
 * 
 */
public class JDiskTracksCorrectedLayer extends JEllipticLayer {

	/**
	 * instrument scale
	 */
	private Scale scale = null;

	public JDiskTracksCorrectedLayer() {
		super();
	}

	public void setScale(Scale scale) {
		this.scale = scale;
	}

	private Point2D.Double realCenter;
	
	public void setRealCenter(Point2D.Double realCenter) {
		this.realCenter = realCenter;
	}
	
	public Point2D.Double getRealCenter() {
		return realCenter;
	}
	
	
	@Override
	public void drawLayer(Graphics2D g2d) {
		super.drawLayer(g2d);

		EllipseParameters params = getCurrentEllipseParameters();
		if (params != null && scale != null) {
			// draw the borders of tracks

			Color oldc = g2d.getColor();
			try {
				g2d.setColor(Color.black);

				// first track low line
				double distance = scale.getFirstTrackAxis()
						- scale.getIntertrackHeight() / 2;

				EllipseParameters workingCopy = params.copy();

				for (int i = 0; i < scale.getTrackNb() + 1; i++) {

					double widthFactor = distance / scale.getWidth();

					Double shape = constructEllipseShapeWithCenterCorrections(
							realCenter, workingCopy, widthFactor);
					g2d.draw(shape);
					distance += scale.getIntertrackHeight();

				}

			} finally {
				g2d.setColor(oldc);
			}

		}

	}

	protected double deformationFactor(double visitedAngle, double widthfactor) {
		return widthfactor;
	}

	protected Path2D.Double constructEllipseShapeWithCenterCorrections(
			Point2D realCenter, EllipseParameters contourEllipse,
			double widthfactor) {

		assert widthfactor >= 0 && widthfactor <= 1.05;
		assert realCenter != null;

		// draw ellipse
		Path2D.Double p = new Path2D.Double();

		for (double visitedAngle = 0; visitedAngle < 2 * Math.PI + 1; visitedAngle += 0.05) {

			// real contour point
			MathVect v = new MathVect(
					contourEllipse.a * Math.cos(visitedAngle), contourEllipse.b
							* Math.sin(visitedAngle));
			MathVect centerContourEllipse = new MathVect(
					contourEllipse.centre.x, contourEllipse.centre.y);
			v = v.rotate(contourEllipse.angle);
			v = v.plus(centerContourEllipse);

			MathVect centerDisk = new MathVect(realCenter.getX(),
					realCenter.getY());

			
			v = v.moins(centerDisk)
					.scale(deformationFactor(visitedAngle, widthfactor))
					.plus(centerDisk);

			if (visitedAngle == 0) {
				p.moveTo(v.getX(), v.getY());
			} else {
				p.lineTo(v.getX(), v.getY());
			}

		}

		return p;

	}
}
