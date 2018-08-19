package org.barrelorgandiscovery.virtualbook.checker;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractScaleConstraint;
import org.barrelorgandiscovery.scale.ConstraintList;
import org.barrelorgandiscovery.scale.ConstraintMinimumHoleLength;
import org.barrelorgandiscovery.scale.ConstraintMinimumInterHoleLength;
import org.barrelorgandiscovery.scale.Scale;


/**
 * Factory Creating checker instance from scale definition
 * 
 * @author Freydiere Patrice
 */
public class CheckerFactory {

	private static Logger logger = Logger.getLogger(CheckerFactory.class);

	/**
	 * the factory method
	 * @param scale the scale
	 * @return an array of checker
	 */
	public static Checker[] createCheckers(Scale scale) {

		logger.debug("createCheckers for gamme " + scale); //$NON-NLS-1$
		if (scale == null)
			return new Checker[0];

		ArrayList<Checker> checkers = new ArrayList<Checker>();

		ConstraintList list = scale.getConstraints();
		if (list == null)
			return new Checker[0];

		for (Iterator<AbstractScaleConstraint> iterator = list.iterator(); iterator
				.hasNext();) {
			AbstractScaleConstraint constraint = iterator.next();

			if (constraint == null)
				continue;

			if (constraint instanceof ConstraintMinimumHoleLength) {
				ConstraintMinimumHoleLength c = (ConstraintMinimumHoleLength) constraint;

				checkers.add(new TooShortHole(c.getMinimumHoleLength()));

			} else if (constraint instanceof ConstraintMinimumInterHoleLength) {

				ConstraintMinimumInterHoleLength c = (ConstraintMinimumInterHoleLength) constraint;

				checkers.add(new InterHoleMinLength(c
						.getMinimumInterHoleLength()));

			} else {
				logger.warn("unknown constraint " //$NON-NLS-1$
						+ constraint.getClass().getName());
			}

		}

		return checkers.toArray(new Checker[0]);
	}

	public static Checker toComposite(Checker[] checkers) {
		return new CompositeChecker(checkers);
	}

}
