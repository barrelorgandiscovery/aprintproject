package org.barrelorgandiscovery.extensionsng.perfo.ng.extension;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLLazerMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLPunchMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.lazer.mock.MockLazerMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.mock.MockMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard.MachineExtensionPoint;

public class MachineExtension implements IExtension, MachineExtensionPoint {

	private static Logger logger = Logger.getLogger(MachineExtension.class);

	private ExtensionPoint[] extensionPoints = null;

	public MachineExtension() {
		try {
			extensionPoints = new ExtensionPoint[] { new SimpleExtensionPoint(MachineExtensionPoint.class, this) };
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public ExtensionPoint[] getExtensionPoints() {
		return extensionPoints;
	}

	public String getName() {
		return "Machine Extension";
	}

	@Override
	public AbstractMachineParameters[] createAllMachineParameters() {

		GRBLPunchMachineParameters grblpunchpachineparameters = new GRBLPunchMachineParameters();
		GRBLLazerMachineParameters grblLazerMachineParameters = new GRBLLazerMachineParameters();
		MockMachineParameters mockMachine = new MockMachineParameters();
		MockLazerMachineParameters mockMachineLazer = new MockLazerMachineParameters();

		return new AbstractMachineParameters[] { grblpunchpachineparameters, grblLazerMachineParameters, mockMachine,
				mockMachineLazer };

	}

}
