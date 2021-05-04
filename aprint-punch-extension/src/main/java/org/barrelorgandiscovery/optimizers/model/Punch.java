package org.barrelorgandiscovery.optimizers.model;

import org.barrelorgandiscovery.optimizers.model.visitor.OptimizedObjectVisitor;
import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * Classe représentant un accoup de la machine à perforer
 * 
 * @author pfreydiere
 */
public class Punch extends OptimizedObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2965791916758203973L;
	
	public double x;
	public double y;

	public Punch(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		int s = HashCodeUtils.hash(HashCodeUtils.SEED, x);
		s = HashCodeUtils.hash(s, y);
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		Punch n = (Punch) obj;
		return x == n.x && y == n.y;
	}

	@Override
	public String toString() {
		return "[" + x + ";" + y + "]";
	}

	@Override
	public Extent getExtent() {
		return new Extent(x, y, x, y);
	}

	@Override
	public double firstX() {
		return x;
	}

	@Override
	public double firstY() {
		return y;
	}

	@Override
	public double lastX() {
		return x;
	}

	@Override
	public double lastY() {
		return y;
	}

	@Override
	public void accept(OptimizedObjectVisitor visitor) {
		visitor.visit(this);
	}

}
