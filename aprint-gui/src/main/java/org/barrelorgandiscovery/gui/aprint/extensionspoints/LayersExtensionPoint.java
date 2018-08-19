package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;

/**
 * This extension point permit the extension to add layers on the display
 * 
 * @author Freydiere Patrice
 * 
 */
public interface LayersExtensionPoint  extends IExtensionPoint{

	/**
	 * Called to add layers to the virtual book component, permit to display
	 * additional objects to the book
	 * 
	 * @param c
	 */
	void addLayers(JVirtualBookScrollableComponent c);

}
