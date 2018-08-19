package org.barrelorgandiscovery.gaerepositoryclient.synchroreport;

public abstract interface SynchroAction {

	public String getMessage();

	public void execute() throws Exception;

}
