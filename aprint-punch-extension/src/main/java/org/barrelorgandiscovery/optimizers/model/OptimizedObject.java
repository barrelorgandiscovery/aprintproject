package org.barrelorgandiscovery.optimizers.model;

import java.io.Serializable;

/**
 * these are objects used in optimization
 * 
 * @author pfreydiere
 *
 */
public abstract class OptimizedObject implements Serializable {

	public abstract Extent getExtent();

	public abstract double firstX();

	public abstract double firstY();

	public abstract double lastX();

	public abstract double lastY();

	/**
	 * additional information to add semantic to objects, 
	 * typically add the layer, or object belonging
	 */
	public String userInformation;

}
