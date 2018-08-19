package org.barrelorgandiscovery.model.execution;

import org.barrelorgandiscovery.model.ModelStep;

/**
 * listener for execution, permit to inform about the execution
 * 
 * @author use
 *
 */
public interface IModelExecutionListener {

	/**
	 * inform the model is to be executed
	 */
	void startExecuteModel();

	/**
	 * executing step
	 * 
	 * @param step
	 */
	void stepExecuting(ModelStep step);

	/**
	 * inform the step has been executed
	 * 
	 * @param step
	 */
	void stepExecuted(ModelStep step);

	/**
	 * inform the model has been executed
	 */
	void endExecuteModel();

}
