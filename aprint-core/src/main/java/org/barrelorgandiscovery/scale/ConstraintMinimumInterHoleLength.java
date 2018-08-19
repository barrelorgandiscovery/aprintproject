package org.barrelorgandiscovery.scale;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * Contrainte d'espace minimum entre les trous
 * 
 * @author Freydiere Patrice
 */
public class ConstraintMinimumInterHoleLength extends AbstractScaleConstraint {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3603226946160995910L;
	private double minimuminterholeLenght;

	public ConstraintMinimumInterHoleLength(double mm) {
		assert !Double.isNaN(mm);
		assert mm > 0;

		this.minimuminterholeLenght = mm;
	}

	public double getMinimumInterHoleLength() {
		return this.minimuminterholeLenght;
	}

	@Override
	public String toString() {
		return Messages.getString("ConstraintMinimumInterHoleLength.0") + String.format("%.2f", getMinimumInterHoleLength()) //$NON-NLS-1$ //$NON-NLS-2$
				+ "mm"; //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		int s = HashCodeUtils.SEED;
		s = HashCodeUtils.hash(s, getClass().getName());
		s = HashCodeUtils.hash(s, minimuminterholeLenght);
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		
		
		ConstraintMinimumInterHoleLength c = (ConstraintMinimumInterHoleLength) obj;
		if (Double.compare(c.minimuminterholeLenght , minimuminterholeLenght) != 0)
			return false;

		return true;
	}

}
