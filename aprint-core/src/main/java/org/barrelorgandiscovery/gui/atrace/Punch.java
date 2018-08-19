package org.barrelorgandiscovery.gui.atrace;

import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * Classe représentant un accoup de la machine à perforer
 * 
 * @author Freydiere Patrice
 */
public class Punch {

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
		return "[" + x + ";" + y +"]";
	}
	
}
