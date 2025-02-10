package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.IStatusBarFeedBackTransactional;

public interface InformStatusBarTransactionalExtensionPoint extends IExtensionPoint {
	/**
	 * inform status bar for display additional actions
	 * 
	 * @param repository
	 */
	public void informStatusBarTransactional(IStatusBarFeedBackTransactional statusbar);
}
