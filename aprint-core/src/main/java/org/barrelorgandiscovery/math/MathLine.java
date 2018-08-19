package org.barrelorgandiscovery.math;

import java.awt.geom.Point2D;

/**
 * simple class for handling line
 * @author pfreydiere
 *
 */
public class MathLine {

	private Point2D.Double ptBase;
	private MathVect vecteur;

	public MathLine(Point2D.Double c, MathVect v) {
		this.ptBase = c;
		this.vecteur = v;
	}

	public MathLine(Point2D.Double p1, Point2D.Double p2) {
		this.ptBase = p1;
		this.vecteur = new MathVect(p1, p2);
	}

	public MathVect getVecteur() {
		return vecteur;
	}

	public double a() {
		return (vecteur.getY() / vecteur.getX());
	}

	public double b() {
		return ptBase.y - a() * ptBase.x;
	}

	public Point2D.Double intersect(MathLine l2) {
		double xintersect = (b() - l2.b()) / (l2.a() - a());
		return new Point2D.Double(xintersect, a() * xintersect + b());
	}

	public Point2D.Double center() {
		return new Point2D.Double(ptBase.x + vecteur.getX() / 2, ptBase.y
				+ vecteur.getY() / 2);
	}

}
