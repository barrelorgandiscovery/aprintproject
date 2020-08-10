package org.barrelorgandiscovery.optimizers.model;

/**
 * these are objects used in optimization
 * 
 * @author pfreydiere
 *
 */
public abstract class OptimizedObject {

	public abstract Extent getExtent();
	
	public abstract double firstX();
	public abstract double firstY();
	public abstract double lastX();
	public abstract double lastY();

}
