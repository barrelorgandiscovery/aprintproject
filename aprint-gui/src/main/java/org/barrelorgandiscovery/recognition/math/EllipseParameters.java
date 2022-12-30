package org.barrelorgandiscovery.recognition.math;

import java.awt.geom.Point2D;
import java.io.Serializable;

import Jama.Matrix;

/**
 * object holding the elipse parameter finding.
 * 
 * @author pfreydiere
 *
 */
public class EllipseParameters implements Serializable {

	public Point2D.Double centre;
	/**
	 * angle en rad
	 */
	public double angle;
	/**
	 * demi grand axe
	 */
	public double a;
	/**
	 * demi axe b
	 */
	public double b;

	public EllipseParameters copy() {
		EllipseParameters c = new EllipseParameters();
		c.centre = centre;
		c.angle = angle;
		c.a = a;
		c.b = b;
		return c;
	}

	/**
	 * compute position on the ellipse using the angle and ratio (length ratio, 1.0
	 * -&gt;  on the ellipse circle).
	 * 
	 * @param a1
	 * @param ratio
	 * @return a Double Point2D
	 */
	public Point2D.Double computePointOnEllipse(double a1, double ratio) {

		Matrix res = computePointOnEllipseMatrix(a1, ratio);

		Point2D.Double p = new Point2D.Double(res.get(0, 0), res.get(1, 0));

		return p;
	}

	/**
	 * Compute a point on a ellipse, using the angle and ratio (length ratio, 1.0 -&gt;
	 * on the ellipse circle).
	 * 
	 * @param a1
	 * @param ratio
	 * @return a Matrix vector
	 */
	public Matrix computePointOnEllipseMatrix(double a1, double ratio) {
		// rotation by angle
		Matrix rotationMatrix = new Matrix(
				new double[][] { { Math.cos(angle), -Math.sin(angle) }, { Math.sin(angle), Math.cos(angle) }

				});

		Matrix center = new Matrix(new double[][] { { centre.x, centre.y, } });

		Matrix vpos = new Matrix(
				new double[][] { { a * ratio * Math.cos(a1 - angle), b * ratio * Math.sin(a1 - angle) } });

		Matrix r = rotationMatrix.times(vpos.transpose());

		Matrix res = r.plus(center.transpose());
		return res;
	}

	/**
	 * Compute a point on a ellipse, using the angle and ratio (length ratio, 1.0 -&gt;
	 * on the ellipse circle)
	 * 
	 * @param a1
	 * @param ratio
	 * @return a Math Vect
	 */
	public MathVect computePointOnEllipseMathVect(double a1, double ratio) {
		Matrix res = computePointOnEllipseMatrix(a1, ratio);
		return new MathVect(res.get(0, 0), res.get(1, 0));
	}

}