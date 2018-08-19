package org.barrelorgandiscovery.scale;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * Contrainte demandant la longueur minimum des trous
 * 
 * @author Freydiere Patrice
 */
public class ConstraintMinimumHoleLength extends AbstractScaleConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3158324448626570354L;
	private double minimumholeLenght;

	public ConstraintMinimumHoleLength(double mm) {
		assert !Double.isNaN(mm);
		assert mm > 0;

		this.minimumholeLenght = mm;
	}

	public double getMinimumHoleLength() {
		return this.minimumholeLenght;
	}

	@Override
	public String toString() {

		return Messages.getString("ConstraintMinimumHoleLength.0") //$NON-NLS-1$
				+ String.format("%.2f", this.minimumholeLenght) + Messages.getString("ConstraintMinimumHoleLength.2"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	@Override
	public int hashCode() {
		int s = HashCodeUtils.SEED;
		s = HashCodeUtils.hash(s, getClass().getName());
		s = HashCodeUtils.hash(s, minimumholeLenght);
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		ConstraintMinimumHoleLength c = (ConstraintMinimumHoleLength) obj;
		if (Double.compare(c.minimumholeLenght, minimumholeLenght) != 0)
			return false;

		return true;
	}

}
