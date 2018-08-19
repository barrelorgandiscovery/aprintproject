package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.APrint;

/**
 * This extension point permit the application to have a reference of the aprint
 * gui
 * 
 * @author Freydiere Patrice
 * 
 */
public interface InitExtensionPoint  extends IExtensionPoint{
	/**
	 * this method is called by aprint when the application initialize
	 * 
	 * @param f
	 */
	void init(APrint f);
}
