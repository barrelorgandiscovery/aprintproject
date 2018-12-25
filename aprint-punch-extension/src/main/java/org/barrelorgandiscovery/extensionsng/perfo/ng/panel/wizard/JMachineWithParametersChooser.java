package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.GUIMachineParametersRepository;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachineParameters;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.prefs.PrefixedNamePrefsStorage;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;

import com.jeta.forms.components.panel.FormPanel;

public class JMachineWithParametersChooser extends JPanel {

  /** */
  private static final long serialVersionUID = 8767588173606262592L;

  private static final String STORAGE_MACHINE_PROPERTIES_DOMAIN =
      "punchextension.machines."; //$NON-NLS-1$

  private static final Logger logger = Logger.getLogger(JMachineWithParametersChooser.class);

  private IPrefsStorage preferences;

  // by default
  private AbstractMachineParameters selectedMachineParameters = new GRBLMachineParameters();

  public JMachineWithParametersChooser(IPrefsStorage ps) throws Exception {
    this.preferences = ps;
    setLayout(new BorderLayout());
    initComponents();
  }

  private JComboBox machineCombo;

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

  public PrefixedNamePrefsStorage constructMachinePreferenceStorage(AbstractMachineParameters gp) {
    PrefixedNamePrefsStorage pps =
        new PrefixedNamePrefsStorage(
            STORAGE_MACHINE_PROPERTIES_DOMAIN + gp.getLabelName(), preferences);
    return pps;
  }

  protected void initComponents() throws Exception {

    InputStream is = getClass().getResourceAsStream("machinechoosecomponent.jfrm"); //$NON-NLS-1$
    assert is != null;
    FormPanel fp = new FormPanel(is);
    add(fp, BorderLayout.CENTER);

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

    machineCombo = fp.getComboBox("cbmachine"); //$NON-NLS-1$
    machineCombo.setModel(new DefaultComboBoxModel<MachineParameterDisplayer>(displayers));

    JButton parametersChoice = (JButton) fp.getButton("btnparameters"); //$NON-NLS-1$
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

  public AbstractMachine getSelectedMachine() throws Exception {
    return selectedMachineParameters.createAssociatedMachineInstance();
  }

  public AbstractMachineParameters getMachineParameters() {
    return selectedMachineParameters;
  }

  public void setSelectedMachineParameters(AbstractMachineParameters machine) {
    this.selectedMachineParameters = machine;

    machineCombo.setSelectedItem(machine);
  }

  public AbstractMachineParameters getSelectedMachineParameters() {
    return selectedMachineParameters;
  }

  public static void main(String[] args) throws Exception {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setSize(300, 100);
    f.getContentPane().setLayout(new BorderLayout());
    FilePrefsStorage p = new FilePrefsStorage(new File("c:\\temp\\testmachinechoose.properties"));
    p.load();

    f.getContentPane().add(new JMachineWithParametersChooser(p), BorderLayout.CENTER);
    f.setVisible(true);
  }
}
