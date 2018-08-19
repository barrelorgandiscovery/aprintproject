package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import javax.swing.JToolBar;

import org.barrelorgandiscovery.extensions.IExtensionPoint;

public interface VirtualBookToolbarButtonsExtensionPoint  extends IExtensionPoint{

	void addButtons(JToolBar tb);

	
}
