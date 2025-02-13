package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.mock;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.BaseAbstractPunchMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GCodeCompiler;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GRBLPunchCompilerVisitor;

public class MockMachine extends BaseAbstractPunchMachine {

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

	@Override
	public GCodeCompiler createNewGCodeCompiler(AbstractMachineParameters parameters) throws Exception {
		return new GRBLPunchCompilerVisitor();
	}

}
