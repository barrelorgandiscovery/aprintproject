package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.generic.lasergcode;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.BaseAbstractLazerMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GCodeCompiler;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GRBLLazerCompilerVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLLazerMachineParameters;

public class GenericLazerMachine extends BaseAbstractLazerMachine {

	@Override
	public String getTitle() {
		return "Generic Machine Not Connected";
	}

	@Override
	public String getDescription() {
		return "Generic Lazer Machine";
	}

	@Override
	public MachineControl open(AbstractMachineParameters parameters) throws Exception {
		return new GenericLazerMachineControl();
	}

	@Override
	public GCodeCompiler createNewGCodeCompiler(AbstractMachineParameters parameters) throws Exception {
		assert parameters instanceof GenericLazerMachineParameters;
		GenericLazerMachineParameters params = (GenericLazerMachineParameters) parameters;

		return new GenericLazerCompilerVisitor(params.getMaxspeed(), params.getMaxPower(),
				params.getGenerationParameters());
	}

}
