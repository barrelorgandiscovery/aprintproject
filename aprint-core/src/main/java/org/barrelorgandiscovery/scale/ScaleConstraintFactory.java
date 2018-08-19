package org.barrelorgandiscovery.scale;

/**
 * Factory for getting all constraints Classes
 * 
 * @author Freydiere Patrice
 * 
 */
public class ScaleConstraintFactory {

	private ScaleConstraintFactory() {

	}

	/**
	 * Get All the classes of constraints associated to the scale
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class[] getScaleConstraintTypeList() {
		return new Class[] { ConstraintMinimumHoleLength.class,
				ConstraintMinimumInterHoleLength.class };
	}

}
