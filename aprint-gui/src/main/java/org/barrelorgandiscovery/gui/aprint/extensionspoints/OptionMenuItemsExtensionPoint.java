package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import javax.swing.JMenu;

import org.barrelorgandiscovery.extensions.IExtensionPoint;

/**
 * This interface permit adding options in the options menu
 * 
 * @author Freydiere Patrice
 * 
 */
public interface OptionMenuItemsExtensionPoint  extends IExtensionPoint{

	void addOptionMenuItem(JMenu options);

}
