package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.mock;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;

public class MockMachine extends AbstractMachine {

	@Override
	public String getTitle() {
		return "Test Machine Not Connected";
	}

	@Override
	public String getDescription() {
		return "MockMachine";
	}

	@Override
	public MachineControl open(AbstractMachineParameters parameters) throws Exception {
		return new MockMachineControl();
	}

}
