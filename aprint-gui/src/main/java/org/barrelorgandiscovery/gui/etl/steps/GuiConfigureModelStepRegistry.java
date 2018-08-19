package org.barrelorgandiscovery.gui.etl.steps;

import org.barrelorgandiscovery.gui.etl.JConfigurePanel;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.TerminalParameterModelStep;
import org.barrelorgandiscovery.model.steps.scripts.GroovyScriptModelStep;
import org.barrelorgandiscovery.model.steps.scripts.GroovyScriptModelUI;

public class GuiConfigureModelStepRegistry {

  private GuiConfigureModelStepRegistry() {}

  public static JConfigurePanel getUIToConfigureStep(
      ModelStep modelStep, JConfigurePanelEnvironment env) {

    assert modelStep != null;

    if (modelStep.getClass() == TerminalParameterModelStep.class) {
      return new JConfigureTerminalParameter((TerminalParameterModelStep) modelStep, env);
    } else if (modelStep.getClass() == GroovyScriptModelStep.class) {
    	return new GroovyScriptModelUI((GroovyScriptModelStep) modelStep);
    }

    return new JDefaultConfigurePanel(modelStep, env);
  }
}
