package org.barrelorgandiscovery.optimizers.model;

import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * Classe repr�sentant un accoup de la machine � perforer
 * 
 * @author Freydiere Patrice
 */
public class Punch extends OptimizedObject {

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

}