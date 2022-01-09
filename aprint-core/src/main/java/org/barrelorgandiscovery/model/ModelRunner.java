package org.barrelorgandiscovery.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.exec.IConsoleLog;
import org.barrelorgandiscovery.exec.IExecute;
import org.barrelorgandiscovery.model.execution.IModelExecutionListener;

/**
 * Class for running a model, take a correctly formed model remember the current
 * execute step, the associated parameter values, permit to run an other step
 * 
 * @author pfreydiere
 * 
 */
public class ModelRunner implements IExecute {

	private static Logger logger = Logger.getLogger(ModelRunner.class);

	private Model currentModel = null;

	private IModelExecutionListener modelExecutionListener;

	/**
	 * construct a model runner
	 */
	public ModelRunner(Model m) throws Exception {
		// model must be scheduled
		scheduledSteps = m.schedule();
		iterator = scheduledSteps.iterator();
		currentStepToBeExecuted = null;
		if (iterator.hasNext())
			currentStepToBeExecuted = iterator.next();
		currentModel = m;
	}

	/**
	 * Remember the values of a link during propagation, if link not found, null
	 * value is passed
	 */
	private Map<ModelLink, Object> linkValues = new HashMap<ModelLink, Object>();

	private Iterator<ModelStep> iterator = null;

	/**
	 * Scheduled steps for execution
	 */
	private Set<ModelStep> scheduledSteps;

	/**
	 * Current executing step
	 */
	private ModelStep currentStepToBeExecuted;

	/**
	 * Restart the execution
	 */
	public void restart() {
		linkValues.clear();
		iterator = scheduledSteps.iterator();
		currentStepToBeExecuted = null;
		if (iterator.hasNext())
			currentStepToBeExecuted = iterator.next();
	}

	/**
	 * is execution finished ?
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return currentStepToBeExecuted == null;
	}

	/**
	 * Execute the current step, and move to the next one if an exception occur in
	 * the execution
	 * 
	 * @throws Exception
	 */
	public void executeCurrentStepAndMoveToNext() throws Exception {

		assert iterator != null;
		assert currentStepToBeExecuted != null;

		// compute incoming valued
		if (currentStepToBeExecuted == null)
			throw new Exception("no more steps to execute");

		logger.debug("get all preceding links");
		Set<ModelLink> precedingLinksAssociatedTo = currentModel.getPrecedingLinksAssociatedTo(currentStepToBeExecuted);

		HashMap<AbstractParameter, Object> inputValuesForStep = new HashMap<AbstractParameter, Object>();
		for (Iterator iterator = precedingLinksAssociatedTo.iterator(); iterator.hasNext();) {
			ModelLink modelLink = (ModelLink) iterator.next();
			AbstractParameter inputParameter = modelLink.getTo();

			// @@@ Merge input , must define a reduce operator
			// here we adopt a last read parameters overwrite

			inputValuesForStep.put(inputParameter, linkValues.get(modelLink));
		}

		logger.debug("execute the step :" + currentStepToBeExecuted);

		Map<AbstractParameter, Object> resultValues = null;
		try {
			if (modelExecutionListener != null) {
				modelExecutionListener.stepExecuting(currentStepToBeExecuted);
			}
			resultValues = currentStepToBeExecuted.execute(inputValuesForStep);
			if (resultValues == null) {
				logger.warn("implementation error, step " + currentStepToBeExecuted + " must return values");
			}
			assert resultValues != null;
			if (modelExecutionListener != null) {
				modelExecutionListener.stepExecuted(currentStepToBeExecuted);
			}
		} catch (Exception ex) {
			if (modelExecutionListener != null) {
				modelExecutionListener.stepExecuted(currentStepToBeExecuted);
			}
			throw new ModelRunnerException(currentStepToBeExecuted, ex);
		}

		logger.debug("OK execution seems to be fine, put the values to the output links parameters (links)");

		Set<ModelLink> followingLinksAssociatedTo = currentModel.getFollowingLinksAssociatedTo(currentStepToBeExecuted);

		for (Iterator iterator = followingLinksAssociatedTo.iterator(); iterator.hasNext();) {
			ModelLink modelLink = (ModelLink) iterator.next();

			linkValues.put(modelLink, resultValues.get(modelLink.getFrom()));
		}

		logger.debug("values propagated, move to next");

		currentStepToBeExecuted = null;
		if (iterator.hasNext()) {
			currentStepToBeExecuted = iterator.next();
		}

	}

	/**
	 * Execute the model
	 * 
	 * @throws Exception
	 */
	public void executeAllSteps() throws Exception {

		if (modelExecutionListener != null) {
			modelExecutionListener.startExecuteModel();
		}

		try {

			while (!isFinished()) {
				executeCurrentStepAndMoveToNext();
			}
			logger.debug("end of all execution");
		} finally {
			if (modelExecutionListener != null) {
				modelExecutionListener.endExecuteModel();
			}
		}
	}

	/**
	 * Return the value of the link given in parameter
	 */
	public Object getValueForLink(ModelLink ml) {
		return linkValues.get(ml);
	}

	/**
	 * return associated model
	 * 
	 * @return
	 */
	public Model getModel() {
		return currentModel;
	}

	/**
	 * define the listener for following the execution
	 * 
	 * @param currentStepToBeExecuted
	 */
	public void setCurrentStepToBeExecuted(ModelStep currentStepToBeExecuted) {
		this.currentStepToBeExecuted = currentStepToBeExecuted;
	}

	/////////////////////////////////////////////////////////////////////
	// IExecute interface handling

	@Override
	public Map<String, Object> execute(Map<String, Object> variables, IConsoleLog console) throws Exception {

		Model model = getModel();
		if (variables != null) {
			for (Entry<String, Object> e : variables.entrySet()) {
				String parameterName = e.getKey();
				TerminalParameterModelStep tp = model.getInTerminalByName(parameterName);
				if (tp != null) {
					logger.debug("assign input parameter :" + e.getKey() + " with value :" + e.getValue());
					// sanity check
					tp.setValue(e.getValue());
				}
			}
		}
		logger.debug("execute steps");
		executeAllSteps();

		logger.debug("execution done");
		ArrayList<TerminalParameterModelStep> allTerminals = model.getTerminalModelStep(true);

		Map<String, Object> rethash = new HashMap<String, Object>();
		if (allTerminals != null) {
			for (TerminalParameterModelStep t : allTerminals) {
				logger.debug("add terminal value :" + t.getName());
				rethash.put(t.getName(), t.getValue());
			}
		}

		return rethash;
	}

	public void setModelExecutionListener(IModelExecutionListener modelExecutionListener) {
		this.modelExecutionListener = modelExecutionListener;
	}

}
