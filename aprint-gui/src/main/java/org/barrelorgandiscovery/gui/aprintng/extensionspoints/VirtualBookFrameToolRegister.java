package org.barrelorgandiscovery.gui.aprintng.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

public interface VirtualBookFrameToolRegister  extends IExtensionPoint{

	/**
	 * Register a toolWindow
	 * 
	 */
	void registerToolWindow(MyDoggyToolWindowManager manager);

}
