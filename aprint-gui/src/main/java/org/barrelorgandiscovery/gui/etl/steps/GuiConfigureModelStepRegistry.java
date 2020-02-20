package org.barrelorgandiscovery.gui.etl.steps;

import java.lang.reflect.Method;

import org.barrelorgandiscovery.gui.etl.JConfigurePanel;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.TerminalParameterModelStep;
import org.barrelorgandiscovery.model.steps.scripts.GroovyScriptModelStep;
import org.barrelorgandiscovery.model.steps.scripts.GroovyScriptModelUI;

public class GuiConfigureModelStepRegistry {

	private GuiConfigureModelStepRegistry() {
	}

	public static JConfigurePanel getUIToConfigureStep(ModelStep modelStep, JConfigurePanelEnvironment env) {

		assert modelStep != null;

		if (modelStep.getClass() == TerminalParameterModelStep.class) {
			return new JConfigureTerminalParameter((TerminalParameterModelStep) modelStep, env);
		} else if (modelStep.getClass() == GroovyScriptModelStep.class) {
			return new GroovyScriptModelUI((GroovyScriptModelStep) modelStep);
		} else {

			try {

				Method m = (modelStep.getClass().getMethod("getUIToConfigureStep", JConfigurePanelEnvironment.class));
				try {
					JConfigurePanel p = (JConfigurePanel) m.invoke(modelStep, env);
					return p;
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			} catch (NoSuchMethodException ex) {
				// continue
			}

		}

		return new JDefaultConfigurePanel(modelStep, env);
	}
}
