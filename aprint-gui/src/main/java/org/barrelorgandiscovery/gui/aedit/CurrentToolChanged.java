package org.barrelorgandiscovery.gui.aedit;

/**
 * Listener interface about the current tool change
 * 
 * @author pfreydiere
 * 
 */
public interface CurrentToolChanged {

	/**
	 * The current tool has changed,
	 * 
	 * @param oldTool
	 *            the old activated tool
	 * @param newTool
	 *            the new tool
	 */
	void currentToolChanged(Tool oldTool, Tool newTool);

}
