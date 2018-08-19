package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.StringContent;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.GUIMachineParametersRepository;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachineParameters;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.prefs.PrefixedNamePrefsStorage;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.barrelorgandiscovery.tools.SwingUtils;

import com.jeta.forms.components.panel.FormPanel;

public class StepChooseMachine extends JPanel implements Step {

  private static final String STORAGE_MACHINE_PROPERTIES_DOMAIN =
      "punchextension.machines."; //$NON-NLS-1$

  private static Logger logger = Logger.getLogger(StepChooseMachine.class);

  // by default
  private AbstractMachineParameters selectedMachineParameters = new GRBLMachineParameters();

  private static class MachineParameterDisplayer {
    private AbstractMachineParameters mp;

    public MachineParameterDisplayer(AbstractMachineParameters parameters) {
      assert parameters != null;
      this.mp = parameters;
    }

    @Override
    public String toString() {
      return mp.getLabelName();
    }
  }

  private IPrefsStorage ps;

  public StepChooseMachine(IPrefsStorage ps) throws Exception {
    this.ps = ps;
    initComponents();
  }

  protected void initComponents() throws Exception {

    setLayout(new BorderLayout());
    FormPanel panel =
        new FormPanel(getClass().getResourceAsStream("choosemachine.jfrm")); //$NON-NLS-1$
    add(
        new JScrollPane(
            panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
        BorderLayout.CENTER);

    StringContent content = new StringContent();
    StringBuilder sb = new StringBuilder();
    sb.append("<html><body>"); //$NON-NLS-1$

    sb.append("<h2>")
        .append(Messages.getString("StepChooseMachine.6")) //$NON-NLS-1$ //$NON-NLS-2$
        .append("</h2>"); //$NON-NLS-1$
    sb.append("<p><center><img src=\"data:image/jpeg;base64,"); //$NON-NLS-1$
    InputStream imageStream = getClass().getResourceAsStream("perfo.jpg"); //$NON-NLS-1$
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    StreamsTools.copyStream(imageStream, baos);

    sb.append(new String(org.apache.commons.codec.binary.Base64.encodeBase64(baos.toByteArray())));
    sb.append("\" ></center></p>") //$NON-NLS-1$
        .append(
            "<p>"
                + //$NON-NLS-1$
                Messages.getString("StepChooseMachine.14") //$NON-NLS-1$
                + Messages.getString("StepChooseMachine.15")
                + "<br/>" //$NON-NLS-1$
                + Messages.getString("StepChooseMachine.16") //$NON-NLS-1$
                + "</p><p>" //$NON-NLS-1$
                + Messages.getString("StepChooseMachine.18")
                + //$NON-NLS-1$
                "<a href=\"http://www.barrel-organ-discovery.org\">http://www.barrel-organ-discovery.org</a>") //$NON-NLS-1$
        .append("</body></html>"); //$NON-NLS-1$

    content.insertString(0, sb.toString());

    final JEditorPane presentation =
        new JEditorPane() {
          @Override
          public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            if (size.getWidth() > 300) {
              size.setSize(300, size.getHeight());
            }
            return size;
          }
        };
    // presentation.setAutoscrolls(true);

    // presentation.setAutoscrolls(true);
    presentation.setContentType("text/html"); //$NON-NLS-1$
    presentation.setEditorKit(new CustomEditorKit());
    presentation.setText(sb.toString());

    presentation.setEditable(false);
    // presentation.setDocument(hdoc);
    // presentation
    //		.setText(Messages.getString("StepChooseMachine.1")); //$NON-NLS-1$
    panel.getFormAccessor().replaceBean("presentation", presentation); //$NON-NLS-1$

    JButton pp = (JButton) panel.getButton("openplan"); //$NON-NLS-1$
    pp.setText(Messages.getString("StepChooseMachine.4")); //$NON-NLS-1$
    pp.setToolTipText(Messages.getString("StepChooseMachine.5")); //$NON-NLS-1$
    pp.setEnabled(false);
    pp.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            openPlan();
          }
        });

    // choose the machine configuration

    assert ps != null;

    GRBLMachineParameters gp = new GRBLMachineParameters();
    try {
      PrefixedNamePrefsStorage pps = constructMachinePreferenceStorage(gp);
      gp.loadParameters(pps);
    } catch (Exception ex) {
      logger.error(
          "error while loading the preference storage for machine :" //$NON-NLS-1$
              + ex.getMessage(),
          ex);
    }

    selectedMachineParameters = gp;

    MachineParameterDisplayer[] displayers =
        new MachineParameterDisplayer[] {new MachineParameterDisplayer(gp)};

    machineCombo = panel.getComboBox("cbmachine"); //$NON-NLS-1$
    machineCombo.setModel(new DefaultComboBoxModel<MachineParameterDisplayer>(displayers));

    JButton parametersChoice = (JButton) panel.getButton("parameterButton"); //$NON-NLS-1$
    parametersChoice.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {

            try {
              // display machine parameters

              GUIMachineParametersRepository guiMachineParametersGUIRepository =
                  new GUIMachineParametersRepository();

              JPanel panelParameter =
                  guiMachineParametersGUIRepository.createMachineParameters(
                      selectedMachineParameters);

              JDialog dialog = new JDialog();
              Container cp = dialog.getContentPane();
              cp.setLayout(new BorderLayout());
              cp.add(panelParameter, BorderLayout.CENTER);
              dialog.setSize(500, 300);
              SwingUtils.center(dialog);

              dialog.setVisible(true);

            } catch (Exception ex) {
              logger.error(
                  "error while opening the machine parameter settings :" //$NON-NLS-1$
                      + ex.getMessage(),
                  ex);
              JMessageBox.showError(null, ex);
            }
          }
        });
  }

  public PrefixedNamePrefsStorage constructMachinePreferenceStorage(AbstractMachineParameters gp) {
    PrefixedNamePrefsStorage pps =
        new PrefixedNamePrefsStorage(STORAGE_MACHINE_PROPERTIES_DOMAIN + gp.getLabelName(), ps);
    return pps;
  }

  private void openPlan() {
    // not implemented yet
  }

  public AbstractMachine getSelectedMachine() throws Exception {
    return selectedMachineParameters.createAssociatedMachineInstance();
  }

  public AbstractMachineParameters getMachineParameters() {
    return selectedMachineParameters;
  }

  @Override
  public String getId() {
    return "choosemachine"; //$NON-NLS-1$
  }

  private Step parentStep;
  private JComboBox machineCombo;

  public Step getParentStep() {
    return parentStep;
  }

  public void setParentStep(Step parent) {
    this.parentStep = parent;
  }

  public String getLabel() {
    return Messages.getString("StepChooseMachine.9"); //$NON-NLS-1$
  }

  public void activate(
      Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
      throws Exception {

    if (state != null) {
      assert state instanceof AbstractMachineParameters;
      selectedMachineParameters = (AbstractMachineParameters) state;
      machineCombo.setSelectedItem(selectedMachineParameters);
    }

    // get the selected machine parameters

  }

  public Serializable unActivateAndGetSavedState() throws Exception {

    // store the machine parameters in preferences
    if (selectedMachineParameters != null) {
      // serializable

      PrefixedNamePrefsStorage pnps = constructMachinePreferenceStorage(selectedMachineParameters);

      try {
        selectedMachineParameters.saveParameters(pnps);
        pnps.save();
      } catch (Exception ex) {
        logger.error(
            "error while saving preferences :" + ex.getMessage(), //$NON-NLS-1$
            ex);
        // continue
      }
    }

    return selectedMachineParameters;
  }

  public boolean isStepCompleted() {
    return true;
  }

  public String getDetails() {
    return Messages.getString("StepChooseMachine.10"); //$NON-NLS-1$
  }

  @Override
  public Icon getPageImage() {
    return null;
  }
}
