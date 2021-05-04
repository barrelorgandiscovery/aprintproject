package org.barrelorgandiscovery.optimizers;

import org.barrelorgandiscovery.optimizers.model.OptimizedObject;

/**
 * this interface permit to report the progress of the optimisation
 * 
 * @author pfreydiere
 * 
 */
public interface OptimizerProgress {

	public void report(double progressIndicator, OptimizedObject[] orderedPunches,
			String message);
	
	
}
