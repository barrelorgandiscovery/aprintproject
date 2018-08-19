package org.barrelorgandiscovery.gui;

/**
 * Interface for long
 * 
 * @author Freydiere Patrice
 * 
 */
public interface ProgressIndicator {

	/**
	 * Method call for progress
	 * 
	 * @param progress
	 *            progress indicator
	 * @param message
	 *            the associated message
	 */
	public void progress(double progress, String message);
	

}
