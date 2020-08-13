package org.barrelorgandiscovery.optimizers.model;

import org.barrelorgandiscovery.tools.HashCodeUtils;

public class CutLine extends OptimizedObject {

	public final double x1;
	public final double y1;
	public final double x2;
	public final double y2;

	public CutLine(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	@Override
	public int hashCode() {
		int s = HashCodeUtils.hash(HashCodeUtils.SEED, x1);
		s = HashCodeUtils.hash(s, y1);
		s = HashCodeUtils.hash(s, x2);
		s = HashCodeUtils.hash(s, y2);
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		CutLine n = (CutLine) obj;
		return x1 == n.x1 && y1 == n.y1 && x2 == n.x2 && y2 == n.y2;
	}
	
	@Override
	public Extent getExtent() {
		
		double xmin = Math.min(x1, x2);
		double ymin = Math.min(y1, y2);
		
		double xmax = Math.max(x1, x2);
		double ymax = Math.max(y1, y2);
		
		return new Extent(xmin,ymin,xmax,ymax);
	}

	@Override
	public double firstX() {
		return x1;
	}

	@Override
	public double firstY() {
		return y1;
	}

	@Override
	public double lastX() {
		return x2;
	}

	@Override
	public double lastY() {
		return y2;
	}
	
	
	
}
