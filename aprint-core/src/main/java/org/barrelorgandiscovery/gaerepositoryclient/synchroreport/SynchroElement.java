package org.barrelorgandiscovery.gaerepositoryclient.synchroreport;

public abstract class SynchroElement {

	public static final int MESSAGE = 0;
	public static final int ERROR = 1;

	/**
	 * Status of the synchro element
	 * 
	 * @return
	 */
	public abstract int getStatus();

	/**
	 * Localized message on the synchronization
	 * 
	 * @return
	 */
	public abstract String getMessage();

	/**
	 * In case of errors, this method return the possible corrective actions to
	 * do
	 * 
	 * @return
	 */
	public abstract SynchroAction[] getAssociatedActions();

}
