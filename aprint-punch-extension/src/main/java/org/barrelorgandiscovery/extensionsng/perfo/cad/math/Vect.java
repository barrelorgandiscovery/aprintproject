package org.barrelorgandiscovery.extensionsng.perfo.cad.math;

import com.vividsolutions.jts.geom.Coordinate;

public class Vect {

	private double x;
	private double y;

	public Vect(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vect(Coordinate p1, Coordinate p2) {
		this((p2.x - p1.x), (p2.y - p1.y));
	}

	public Vect orthogonal() {
		return new Vect(-y, x);
	}

	public double norme() {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	public Vect orthoNorme() {
		double n = norme();
		return new Vect(x / n, y / n);
	}

	public Vect moins() {
		return new Vect(-x, -y);
	}

	public Vect plus(Vect v) {
		return new Vect(x + v.x, y + v.y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Coordinate plus(Coordinate p) {
		return new Coordinate(p.x + x, p.y + y);
	}

	public Line lineFrompoint(Coordinate p) {
		return new Line(p, this);
	}

	public Vect scale(double s) {
		return new Vect(x * s, y * s);
	}

	public double vectorielZ(Vect v) {
		return x * v.y - v.x * y;
	}

	public double angleOrigine() {
		return Math.atan2(y, x);
	}

	public double angle(Vect v) {
		return v.angleOrigine() - angleOrigine();
	}

	public Vect rotateOrigin(double angle) {
		return new Vect(x * Math.cos(angle) - y * Math.sin(angle), +x
				* Math.sin(angle) + y * Math.cos(angle));
	}

}
