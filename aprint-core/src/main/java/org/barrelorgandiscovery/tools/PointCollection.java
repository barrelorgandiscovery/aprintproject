package org.barrelorgandiscovery.tools;

import java.util.Vector;

public class PointCollection {

	protected Vector<Point> col = new Vector<Point>();

	public PointCollection() {

	}

	public void addPoint(Point pt) {
		col.add(pt);
	}

	public int size() {
		return col.size();
	}

	public Point get(int index) {
		return col.get(index);
	}

}
