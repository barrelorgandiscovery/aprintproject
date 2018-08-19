package org.barrelorgandiscovery.gui.aprintng.extensionspoints;

import javax.swing.JMenu;

import org.barrelorgandiscovery.extensions.IExtensionPoint;

/**
 * This interface or extension point permit to add elements to the tool button
 * 
 * @author use
 * 
 */
public interface ToolMenuExtensionPoint  extends IExtensionPoint{

	/**
	 * Called for adding menu items in the tools menu of APrintNG
	 * @param menu
	 */
	public void addMenuItem(JMenu menu);

}
