package org.barrelorgandiscovery.recognition.math;

import java.awt.geom.Point2D;
import java.io.Serializable;

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

}