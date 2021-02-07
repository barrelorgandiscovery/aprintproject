package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;

/**
 * Extension Point implemented for providing new Machines for aprint
 * 
 * @author pfreydiere
 *
 */
public interface MachineExtensionPoint  extends IExtensionPoint {

	/**
	 * create allParameters associated to machine
	 * @return
	 */
	public AbstractMachineParameters[] createAllMachineParameters();

}
