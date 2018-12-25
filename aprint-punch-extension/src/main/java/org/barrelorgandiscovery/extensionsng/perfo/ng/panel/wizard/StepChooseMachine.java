package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.StringContent;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.prefs.PrefixedNamePrefsStorage;
import org.barrelorgandiscovery.tools.StreamsTools;

import com.jeta.forms.components.panel.FormPanel;

public class StepChooseMachine extends JPanel implements Step {

  private static Logger logger = Logger.getLogger(StepChooseMachine.class);
 
  private IPrefsStorage ps;
  
  private JMachineWithParametersChooser machineChoose;

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

    machineChoose = new JMachineWithParametersChooser(ps);
    panel.getFormAccessor().replaceBean("lblmachinechoose", machineChoose);
    
    
    assert ps != null;

  
  }


  private void openPlan() {
    // not implemented yet
  }


  @Override
  public String getId() {
    return "choosemachine"; //$NON-NLS-1$
  }

  private Step parentStep;
 
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
      machineChoose.setSelectedMachineParameters((AbstractMachineParameters) state);
    }

    // get the selected machine parameters

  }

  public Serializable unActivateAndGetSavedState() throws Exception {

    // store the machine parameters in preferences
    AbstractMachineParameters selectedMachineParameters = machineChoose.getSelectedMachineParameters();
  if (selectedMachineParameters != null) {
      // serializable

      PrefixedNamePrefsStorage pnps = machineChoose.constructMachinePreferenceStorage(selectedMachineParameters);

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
  
  public AbstractMachine getSelectedMachine() throws Exception {
	  // delegate
	  return machineChoose.getSelectedMachine();
  }
  
  public AbstractMachineParameters getMachineParameters() throws Exception {
	  // delegate
	  return machineChoose.getSelectedMachineParameters();
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
