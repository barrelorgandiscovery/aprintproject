package org.barrelorgandiscovery.gui;

import java.util.concurrent.atomic.AtomicBoolean;

public class CancelTracker implements ICancelTracker {

	private AtomicBoolean cancelState = new AtomicBoolean();
	
	/* (non-Javadoc)
	 * @see fr.freydierepatrice.gui.ICancelTracker#cancel()
	 */
	public boolean  cancel()
	{
		return cancelState.getAndSet(true);
	}
	
	/* (non-Javadoc)
	 * @see fr.freydierepatrice.gui.ICancelTracker#isCanceled()
	 */
	public boolean isCanceled()
	{
		return cancelState.get();
	}
	
	
}
