package org.barrelorgandiscovery.gui.wizard;

public interface StepBeforeChanged {

	/**
	 * called before a step changed return false if the step cannot be changed,
	 * or true otherwise
	 * 
	 * @param step
	 * @return
	 * @throws Exception
	 */
	public boolean beforeStepChanged(Step oldStep, Step newStep, Wizard wizard) throws Exception;

}
