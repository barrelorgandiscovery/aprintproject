package org.barrelorgandiscovery.gui.ascale.constraints;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.ConstraintMinimumHoleLength;
import org.barrelorgandiscovery.scale.ConstraintMinimumInterHoleLength;


/**
 * Class permitting creating a component linked to a constraint class
 * 
 * @author Freydiere Patrice
 * 
 */
public class ConstraintPanelFactory {

	private static Logger logger = Logger
			.getLogger(ConstraintPanelFactory.class);

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private static HashMap<Class, Class> registry = null;

	private ConstraintPanelFactory() {

	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private static void populateRegistry() {
		registry = new HashMap<Class, Class>();

		registry.put(ConstraintMinimumHoleLength.class,
				MinimumHoleLengthConstraintComponent.class);
		registry.put(ConstraintMinimumInterHoleLength.class,
				MinimumInnerHoleLengthComponent.class);
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public static AbstractScaleConstraintComponent getComponentAssociatedToConstraint(
			Class type) throws Exception {

		checkRepository();

		if (registry.containsKey(type)) {

			Class clazz = registry.get(type);
			return (AbstractScaleConstraintComponent) clazz.newInstance();
		}

		return null;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public static AbstractScaleConstraintComponent[] getAllComponents() {
		checkRepository();

		Vector<AbstractScaleConstraintComponent> v = new Vector<AbstractScaleConstraintComponent>();
		for (Iterator<Entry<Class, Class>> iterator = registry.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<Class, Class> type = iterator.next();

			Class clazz = type.getValue();

			try {
				AbstractScaleConstraintComponent a = (AbstractScaleConstraintComponent) clazz
						.newInstance();

				v.add(a);
			} catch (Exception ex) {
				logger.error("getAllComponents", ex); //$NON-NLS-1$
			}

		}
		return v.toArray(new AbstractScaleConstraintComponent[0]);
	}

	private static void checkRepository() {
		synchronized (ConstraintPanelFactory.class) {
			if (registry == null)
				populateRegistry();
		}
	}
}
