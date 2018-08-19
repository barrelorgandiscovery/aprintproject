package org.barrelorgandiscovery.extensionsng.perfo.dxf.math;

import com.vividsolutions.jts.geom.Coordinate;

public class Line {

	private Coordinate ptBase;
	private Vect vecteur;

	public Line(Coordinate c, Vect v) {
		this.ptBase = c;
		this.vecteur = v;
	}
	
	public Line(Coordinate p1, Coordinate p2)
	{
		this.ptBase = p1;
		this.vecteur = new Vect(p1,p2);
	}

	public double a() {
		return (vecteur.getY() / vecteur.getX());
	}

	public double b() {
		return ptBase.y - a() * ptBase.x;
	}

	public Coordinate intersect(Line l2) {
		double xintersect = (b() - l2.b()) / (l2.a() - a());
		return new Coordinate(xintersect, a() * xintersect + b());
	}

}
