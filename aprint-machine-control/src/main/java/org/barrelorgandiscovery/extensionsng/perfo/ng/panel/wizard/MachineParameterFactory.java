package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPointProvider;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;

/**
 * this class discover the machine parameters, parameters permit to create
 * associated machine object, making it extensible to add new machines
 * 
 * @author pfreydiere
 *
 */
public class MachineParameterFactory {

	private static Logger logger = Logger.getLogger(MachineParameterFactory.class);

	private IExtension[] extensionManager;

	public MachineParameterFactory(IExtension[] extensions) {
		this.extensionManager = extensions;
	}

	public AbstractMachineParameters[] createAllMachineParameters() {

		if (extensionManager == null || extensionManager.length <= 0) {
			return new AbstractMachineParameters[0];
		}
		ArrayList<AbstractMachineParameters> machineParameters = new ArrayList<>();

		MachineExtensionPoint[] allMachineExtensionPoint = ExtensionPointProvider
				.getAllPoints(MachineExtensionPoint.class, extensionManager);

		if (allMachineExtensionPoint != null) {
			for (MachineExtensionPoint p : allMachineExtensionPoint) {
				if (p != null) {
					logger.debug("create parameter for extension point " + p);
					try {
						AbstractMachineParameters[] allMachineParameters = p.createAllMachineParameters();
						for (AbstractMachineParameters paramsInstance : allMachineParameters) {
							if (paramsInstance != null) {
								logger.info("adding machine parameters :" + paramsInstance);
								machineParameters.add(paramsInstance);
							}
						}
					} catch (Throwable t) {
						logger.error("error in creating machine from extension point " + p + ":" + t.getMessage(), t);
					}
				}
			}
		}

		return machineParameters.toArray(new AbstractMachineParameters[0]);
	}

}
