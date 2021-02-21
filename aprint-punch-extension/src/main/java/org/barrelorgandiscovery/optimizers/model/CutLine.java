package org.barrelorgandiscovery.optimizers.model;

import org.barrelorgandiscovery.optimizers.model.visitor.OptimizedObjectVisitor;
import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * cut line object
 * 
 * @author pfreydiere
 */
public class CutLine extends OptimizedObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5699811752930400308L;
	
	public final double x1;
	public final double y1;
	public final double x2;
	public final double y2;
	public double powerFraction;
	public double speedFraction;

	public CutLine(double x1, double y1, double x2, double y2, double powerFraction, double speedFraction) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.powerFraction = powerFraction;
		this.speedFraction = speedFraction;
	}

	@Override
	public int hashCode() {
		int s = HashCodeUtils.hash(HashCodeUtils.SEED, x1);
		s = HashCodeUtils.hash(s, y1);
		s = HashCodeUtils.hash(s, x2);
		s = HashCodeUtils.hash(s, y2);
		s = HashCodeUtils.hash(s, powerFraction);
		s = HashCodeUtils.hash(s, speedFraction);
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		CutLine n = (CutLine) obj;
		return x1 == n.x1 && y1 == n.y1 && x2 == n.x2 && y2 == n.y2 
				&& powerFraction == n.powerFraction && speedFraction == n.speedFraction
				;
	}

	@Override
	public Extent getExtent() {

		double xmin = Math.min(x1, x2);
		double ymin = Math.min(y1, y2);

		double xmax = Math.max(x1, x2);
		double ymax = Math.max(y1, y2);

		return new Extent(xmin, ymin, xmax, ymax);
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
	
	@Override
	public void accept(OptimizedObjectVisitor visitor) {
		visitor.visit(this);
	}

}
