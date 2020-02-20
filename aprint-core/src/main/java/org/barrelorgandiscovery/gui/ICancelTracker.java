package org.barrelorgandiscovery.gui;

/**
 * this interface specify an object can use to be aware of 
 * a cancellation (processing, user cancel).
 * this permit the object to properly quit the process and terminate
 * associated resources
 * 
 * @author pfreydiere
 *
 */
public interface ICancelTracker {

	public abstract boolean cancel();

	public abstract boolean isCanceled();

}