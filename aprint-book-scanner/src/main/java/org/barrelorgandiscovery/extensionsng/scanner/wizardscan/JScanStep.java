package org.barrelorgandiscovery.extensionsng.scanner.wizardscan;

import java.io.Serializable;

import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;

public class JScanStep extends BasePanelStep {

  public JScanStep(Step parent) {
    super("scan", parent);
  }

  @Override
  public String getLabel() {
    return "Launch Scan";
  }

  @Override
  public void activate(
      Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
      throws Exception {
	  
  }

  @Override
  public Serializable unActivateAndGetSavedState() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isStepCompleted() {
    return true;
  }
}
