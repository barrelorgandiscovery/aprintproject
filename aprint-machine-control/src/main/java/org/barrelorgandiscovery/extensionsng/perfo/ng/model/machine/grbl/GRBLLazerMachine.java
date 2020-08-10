package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.BaseAbstractLazerMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GRBLLazerCompilerVisitor;

/**
 * GRBLMachine using punch
 * 
 * @author pfreydiere
 *
 */
public class GRBLLazerMachine extends BaseAbstractLazerMachine {

	public GRBLLazerMachine() {

	}

	@Override
	public String getTitle() {
		return "GRBL Lazer machine";
	}

	@Override
	public String getDescription() {
		return "GRBL Lazer command machine";
	}

	@Override
	public MachineControl open(AbstractMachineParameters parameters) throws Exception {

		assert parameters != null;
		assert parameters instanceof GRBLLazerMachineParameters;

		GRBLLazerMachineParameters p = (GRBLLazerMachineParameters) parameters;

		GRBLMachineControl gmc = new GRBLMachineControl(p.getPort(), new GRBLLazerCompilerVisitor(p.getMaxspeed()));
		return gmc;
	}




}
