package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;

public class GRBLMachine extends AbstractMachine {

	public GRBLMachine() {

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
	public MachineControl open(AbstractMachineParameters parameters)
			throws Exception {

		assert parameters != null;
		assert parameters instanceof GRBLMachineParameters;

		GRBLMachineParameters p = (GRBLMachineParameters) parameters;

		GRBLMachineControl gmc = new GRBLMachineControl(p.getPort());
		return gmc;
	}

}
