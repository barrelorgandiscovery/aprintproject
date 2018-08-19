package org.barrelorgandiscovery.gui.wizard;

import java.io.Serializable;

import javax.swing.Icon;

/**
 * Wizard step, implement an action in the wizard, all implementation of step,
 * must derive from JComponent, for the display
 * 
 * @author pfreydiere
 * 
 */
public interface Step {

	/**
	 * get id of the step (for state persistance)
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * get the parent step
	 * 
	 * @return
	 */
	public Step getParentStep();

	/**
	 * set the parent step
	 * 
	 * @param parent
	 */
	public void setParentStep(Step parent);

	/**
	 * Current step label
	 */
	public String getLabel();

	/**
	 * activate the step, the serializable state is the current state of the
	 * step,
	 * 
	 * 
	 * @param state
	 *            the initial state to fill the panel
	 * @param allStepsStates
	 *            get all the steps permitting to gather information about
	 *            previous panels
	 * 
	 * @param stepListener
	 *            a listener that must be called for status changed
	 * 
	 * @throws Exception
	 */
	public void activate(Serializable state, WizardStates allStepsStates,
			StepStatusChangedListener stepListener) throws Exception;

	/**
	 * unactivate the wizzard, and save the step state in the given state
	 * 
	 * @throws Exception
	 */
	public Serializable unActivateAndGetSavedState() throws Exception;

	/**
	 * can we go to the next element, in other terms, do we have all the
	 * elements to go further
	 * 
	 * @return
	 */
	public boolean isStepCompleted();

	/**
	 * get a small explaination of the step
	 * 
	 * @return
	 */
	public String getDetails();

	/**
	 * get the step icon, if return null, the default image is used
	 * 
	 * @return
	 */
	public Icon getPageImage();

}
