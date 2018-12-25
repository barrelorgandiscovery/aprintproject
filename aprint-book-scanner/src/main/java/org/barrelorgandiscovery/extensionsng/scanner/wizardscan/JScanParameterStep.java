package org.barrelorgandiscovery.extensionsng.scanner.wizardscan;

import java.awt.BorderLayout;
import java.io.InputStream;
import java.io.Serializable;

import org.barrelorgandiscovery.extensionsng.scanner.scan.JChooseWebCam;
import org.barrelorgandiscovery.extensionsng.scanner.scan.JTriggerComponent;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

import com.jeta.forms.components.panel.FormPanel;

public class JScanParameterStep extends BasePanelStep {

  /** */
  private static final long serialVersionUID = -7296047711004950598L;

  private IPrefsStorage preferences;

private JChooseWebCam webcamChooser;

private JTriggerComponent jTriggerComponent;

  public JScanParameterStep(Step parent, IPrefsStorage preferences) throws Exception {
    super("scanparameter", parent);
    this.preferences = preferences;
    initComponents();
  }

  protected void initComponents() throws Exception {
    InputStream isform = getClass().getResourceAsStream("parameterpanel.jfrm");
    assert isform != null;
    FormPanel fp = new FormPanel(isform);

    webcamChooser = new JChooseWebCam();
    jTriggerComponent = new JTriggerComponent(preferences);

    fp.getFormAccessor().replaceBean("lblwebcam", webcamChooser);
    fp.getFormAccessor().replaceBean("lbltrigger", jTriggerComponent);

    setLayout(new BorderLayout());
    add(fp, BorderLayout.CENTER);
  }

  @Override
  public String getLabel() {
    return "Choose scan parameters";
  }

  @Override
  public boolean isStepCompleted() {

    return true;
  }

  @Override
  public void activate(
      Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
      throws Exception {}

  @Override
  public Serializable unActivateAndGetSavedState() throws Exception {

    return null;
  }
}
