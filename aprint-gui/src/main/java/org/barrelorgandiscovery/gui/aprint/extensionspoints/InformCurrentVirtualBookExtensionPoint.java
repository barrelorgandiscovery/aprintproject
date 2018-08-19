package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * This extension point permit to an extension to know what is the displayed
 * book, and permit on the fly transformations
 * 
 * @author Freydiere Patrice
 * 
 */
public interface InformCurrentVirtualBookExtensionPoint  extends IExtensionPoint {

	/**
	 * This method is called by aprint to pass the virtual book reference, this
	 * permit the extension to know what is the currently displayed book.
	 * 
	 * You can also make modifications on the virtual book before using in the
	 * software
	 * 
	 * @param vb
	 */
	void informCurrentVirtualBook(VirtualBook vb);

}
