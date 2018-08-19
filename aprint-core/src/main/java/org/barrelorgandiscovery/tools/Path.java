package org.barrelorgandiscovery.tools;

import java.util.Vector;

public class Path extends PointCollection {

	public Path() {

	}

	/**
	 * find all intersections with a y line
	 * 
	 * @param y
	 * @return
	 */
	public Point[] findIntersections(double y) {

		if (col.size() < 2)
			return new Point[0];

		Vector<Point> retvalue = new Vector<Point>();
		for (int i = 0; i < col.size() - 1; i++) {
			Point r = intersect(col.get(i), col.get(i + 1), y);
			if (r != null)
				retvalue.add(r);
		}

		Point[] t = new Point[retvalue.size()];
		retvalue.copyInto(t);

		return t;
	}

	/**
	 * find intersection with the segment
	 * 
	 * @param p1
	 *            first point
	 * @param p2
	 *            second point
	 * @return
	 */
	private Point intersect(Point p1, Point p2, double y) {

		if (p1.getX() == p2.getX())
			return null;

		double a = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());

		double estimatex = (y - p1.getY()) / a + p1.getX();

		double minx = Math.min(p1.getX(), p2.getX());
		double maxx = Math.max(p1.getX(), p2.getX());

		if (estimatex > minx && estimatex < maxx) {
			return new Point(estimatex, y);
		}

		return null;
	}

}
