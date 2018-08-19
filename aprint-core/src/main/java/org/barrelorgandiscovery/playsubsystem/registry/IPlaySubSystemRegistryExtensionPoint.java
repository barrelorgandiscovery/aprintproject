package org.barrelorgandiscovery.playsubsystem.registry;

import org.barrelorgandiscovery.extensions.IExtensionPoint;

/**
 * Extension point, permitting extension to provide playsubsystems
 * 
 * @author use
 */
public interface IPlaySubSystemRegistryExtensionPoint  extends IExtensionPoint{

	/**
	 * Create and get the play sub systems, permit to APrint Studio 
	 * to get all the externals play subsystems
	 * 
	 * @return an array of the play subsystems
	 */
	PlaySubSystemDef[] getPlaySubSystems();

}
