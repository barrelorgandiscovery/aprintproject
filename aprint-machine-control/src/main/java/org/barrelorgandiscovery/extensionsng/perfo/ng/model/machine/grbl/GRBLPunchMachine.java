package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.BaseAbstractPunchMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GRBLPunchCompilerVisitor;

/**
 * GRBLMachine using punch
 * 
 * @author pfreydiere
 *
 */
public class GRBLPunchMachine extends BaseAbstractPunchMachine {

	public GRBLPunchMachine() {

	}

	@Override
	public String getTitle() {
		return Messages.getString("GRBLMachine.0"); //$NON-NLS-1$
	}

	@Override
	public String getDescription() {
		return Messages.getString("GRBLMachine.1"); //$NON-NLS-1$
	}

	@Override
	public MachineControl open(AbstractMachineParameters parameters) throws Exception {

		assert parameters != null;
		assert parameters instanceof GRBLPunchMachineParameters;

		GRBLPunchMachineParameters p = (GRBLPunchMachineParameters) parameters;

		GRBLMachineControl gmc = new GRBLMachineControl(p.getPort(), new GRBLPunchCompilerVisitor());
		return gmc;
	}



}
