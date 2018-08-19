package org.barrelorgandiscovery.gui.wizard;

import java.io.Serializable;

/**
 * Wizzard listener to inform the current step has changed
 * 
 * @author use
 *
 */
public interface StepChanged {

	/**
	 * signal the current step has changed
	 * 
	 * @param stepNo
	 * @param state
	 */
	public void currentStepChanged(int stepNo, Serializable state);

}
