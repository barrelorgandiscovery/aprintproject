package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

import javax.swing.JPanel;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLPunchMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.generic.lasergcode.GenericLazerMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.generic.lasergcode.GenericLazerMachineParametersPanel;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLLazerMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLLazerMachineParametersPanel;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachineParametersPanel;

/**
 * repository to create associated panel to the machine parameters
 * 
 * @author pfreydiere
 *
 */
public class GUIMachineParametersRepository {

	public GUIMachineParametersRepository() {
	}

	public JPanel createMachineParameters(AbstractMachineParameters parameters) throws Exception {

		if (parameters instanceof GRBLPunchMachineParameters) {
			return new GRBLMachineParametersPanel((GRBLPunchMachineParameters) parameters);
		}

		if (parameters instanceof GRBLLazerMachineParameters) {
			return new GRBLLazerMachineParametersPanel((GRBLLazerMachineParameters) parameters);
		}
		
		if (parameters instanceof GenericLazerMachineParameters) {
			return new GenericLazerMachineParametersPanel((GenericLazerMachineParameters) parameters);
		}

		return null; // not found
	}
}
