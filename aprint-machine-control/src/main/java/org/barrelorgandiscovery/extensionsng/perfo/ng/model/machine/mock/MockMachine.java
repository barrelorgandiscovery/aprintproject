package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.BaseAbstractPunchMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;

public class MockMachine extends AbstractMachine {

	@Override
	public List<Class> getAvailableOptimizerClasses() {
		ArrayList<Class> l = new ArrayList<Class>(Arrays.asList(BaseAbstractPunchMachine.punchOptimizersClasses));
		return Collections.unmodifiableList(l);
	}

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
