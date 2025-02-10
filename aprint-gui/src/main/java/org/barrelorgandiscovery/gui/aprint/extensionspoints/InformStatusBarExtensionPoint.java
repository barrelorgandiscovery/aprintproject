package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.IStatusBarFeedback;

public interface InformStatusBarExtensionPoint extends IExtensionPoint {
	/**
	 * inform status bar for display additional actions
	 * 
	 * @param repository
	 */
	public void informStatusBar(IStatusBarFeedback statusbar);
}
