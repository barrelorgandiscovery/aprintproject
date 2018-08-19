package org.barrelorgandiscovery.gui.aprintng.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensions.IExtensionPoint;

/**
 * Extension points for the virtualbook window
 * 
 * @author Freydiere Patrice
 * 
 */
public interface VirtualBookFrameExtensionPoints  extends IExtensionPoint{

	/**
	 * get a New extension instance for the Virtualbook Frame
	 * 
	 * @return
	 */
	IExtension newExtension();

}
