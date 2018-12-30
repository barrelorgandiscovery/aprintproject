package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

import javax.swing.JPanel;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachineParametersPanel;

public class GUIMachineParametersRepository {

  public GUIMachineParametersRepository() {}

  public JPanel createMachineParameters(AbstractMachineParameters parameters) throws Exception {

    if (parameters instanceof GRBLMachineParameters) {
      return new GRBLMachineParametersPanel((GRBLMachineParameters) parameters);
    }
    return null; // not found
  }
}
