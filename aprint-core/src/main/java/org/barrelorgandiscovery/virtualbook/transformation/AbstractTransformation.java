package org.barrelorgandiscovery.virtualbook.transformation;

import org.barrelorgandiscovery.scale.Scale;

public abstract class AbstractTransformation {

	/**
	 * get the Translation Name
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Get the Destination Scale
	 * 
	 * @return
	 */
	public abstract Scale getScaleDestination();

}
