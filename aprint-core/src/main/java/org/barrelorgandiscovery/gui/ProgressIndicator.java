package org.barrelorgandiscovery.gui;

/**
 * Interface for informing about progress
 * 
 * @author pfreydiere
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
