package org.barrelorgandiscovery.gui.wizard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * a state collection for the wizard
 * 
 * @author use
 * 
 */
public class WizardStates implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5671965025981003312L;

	private static Logger logger = Logger.getLogger(WizardStates.class);

	private Map<String, Serializable> states = new HashMap<String, Serializable>();

	public WizardStates() {
	}

	public WizardStates(Serializable initialState) {
		if (initialState != null) {
			try {

				states = (Map<String, Serializable>) initialState;
			} catch (Exception ex) {
				logger.error(ex);
			}

		}
	}

	public void setState(Step step, Serializable ser) {
		assert step != null;
		states.put(step.getId(), ser);
	}

	public Serializable getState(Step step) {
		assert step != null;
		return getState(step.getId());
	}

	public Serializable getState(String stepId) {
		assert stepId != null;
		return states.get(stepId);
	}

	/**
	 * search a previous state that implement a typical interface, starting search
	 * from the parent's currentStep
	 * 
	 * @param currentStep
	 * @param clazz
	 * @return
	 */
	public <T> T getPreviousStateImplementing(Step currentStep, Class<T> clazz) {
		assert currentStep != null;
		return getInPreviousStates(currentStep.getParentStep(), clazz);
	}

	/**
	 * search a previous state that implement a typical state interface, starting
	 * search from the currentStep
	 * 
	 * @param currentStep
	 * @param clazz
	 * @return
	 */
	public <T> T getInPreviousStates(Step currentStep, Class<T> clazz) {
		assert currentStep != null;
		assert clazz != null;
		Step current = currentStep;
		while (current != null) {
			logger.debug("current evaluated step :" + current);
			Serializable s = getState(current);
			if (s == null) {
				logger.debug("not state for step " + current);
			} else {
				logger.debug("state found " + s + " , check if assignable from " + clazz);
				if (clazz.isAssignableFrom(s.getClass())) {
					logger.debug("yes state is assignable");
					return (T) s;
				}
				logger.debug("no it is not");
			}
			current = current.getParentStep();
		}
		logger.warn("no state found for " + clazz);
		return null;
	}

}
