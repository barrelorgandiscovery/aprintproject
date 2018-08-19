package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import javax.swing.JToolBar;

import org.barrelorgandiscovery.extensions.IExtensionPoint;

public interface ToolbarAddExtensionPoint extends IExtensionPoint {

	public JToolBar[] addToolBars();

}
