package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import javax.swing.JMenu;

import org.barrelorgandiscovery.extensions.IExtensionPoint;

public interface HelpMenuItemsExtensionPoint extends IExtensionPoint {
	void addHelpMenuItem(JMenu helpMenu);
}
