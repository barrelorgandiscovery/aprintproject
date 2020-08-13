package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

import javax.swing.JPanel;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLPunchMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachineParametersPanel;

/**
 * repository to create associated panel to the machine parameters
 * @author pfreydiere
 *
 */
public class GUIMachineParametersRepository {

  public GUIMachineParametersRepository() {}

  public JPanel createMachineParameters(AbstractMachineParameters parameters) throws Exception {

    if (parameters instanceof GRBLPunchMachineParameters) {
      return new GRBLMachineParametersPanel((GRBLPunchMachineParameters) parameters);
    }
    return null; // not found
  }
}
