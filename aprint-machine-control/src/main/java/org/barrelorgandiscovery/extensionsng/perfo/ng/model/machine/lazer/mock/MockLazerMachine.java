package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.lazer.mock;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.BaseAbstractLazerMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GCodeCompiler;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GRBLLazerCompilerVisitor;

public class MockLazerMachine extends BaseAbstractLazerMachine {

	@Override
	public String getTitle() {
		return "Test Machine Not Connected";
	}

	@Override
	public String getDescription() {
		return "MockLazerMachine";
	}

	@Override
	public MachineControl open(AbstractMachineParameters parameters) throws Exception {
		return new MockLazerMachineControl();
	}

	@Override
	public GCodeCompiler createNewGCodeCompiler(AbstractMachineParameters parameters) throws Exception {
		return new GRBLLazerCompilerVisitor(100, 100);
	}

}
