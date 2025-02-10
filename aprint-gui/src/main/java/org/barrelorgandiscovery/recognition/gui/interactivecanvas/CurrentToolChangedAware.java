package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;

/**
 * this interface inform a component can be used for been aware of tool changes
 * @author pfreydiere
 *
 */
public interface CurrentToolChangedAware {

	/**
	 * add a listener for the tool change
	 * @param listener
	 */
	public void addCurrentToolChangedListener(CurrentToolChanged listener);

	/** 
	 * remove the listener reference
	 * @param listener
	 */
	public void removeCurrentToolChangedListener(CurrentToolChanged listener);

}