package org.barrelorgandiscovery.gui.aprintng.extensionspoints;

import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookInternalFrame;

public interface InformVirtualBookFrameExtensionPoint {

	/**
	 * this extension inform virtualbook extensions of the frame services
	 */
	void informVirtualBookFrame(APrintNGVirtualBookFrame frame);

}
