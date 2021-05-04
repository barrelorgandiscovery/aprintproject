package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.BorderLayout;
import java.io.File;
import java.io.InputStream;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.barrelorgandiscovery.bookimage.PerfoScanFolder;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensionsng.perfo.ng.extension.MachineExtension;
import org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard.JMachineWithParametersChooser;
import org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard.MachineParameterFactory;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.ITriggerFactory;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.MachineTrigger;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.TimeTrigger;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.Trigger;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.tools.SwingUtils;

import com.github.sarxos.webcam.Webcam;
import com.jeta.forms.components.panel.FormPanel;

public class JTriggerComponent extends JPanel {

  /** */
  private static final long serialVersionUID = 6477801093587688586L;

  private IPrefsStorage ps;
  private ButtonGroup rdgroup;
  private JRadioButton rdTime;
  private JRadioButton rdMachine;
  private JMachineWithParametersChooser chooser;
  private JTextField txtseconds;
  
  private IExtension[] extension;

  public JTriggerComponent(IPrefsStorage ps, IExtension[] extensions) throws Exception {
    this.ps = ps;
    this.extension = extensions;
    initComponents();
  }

  /**
   * instantiate the components
   *
   * @throws Exception
   */
  protected void initComponents() throws Exception {

    InputStream formIs = getClass().getResourceAsStream("triggerparameters.jfrm");//$NON-NLS-1$
    assert formIs != null;
    FormPanel fp = new FormPanel(formIs);

    // rework, only base extensions are defined
    MachineParameterFactory factory = new MachineParameterFactory(extension);
	chooser = new JMachineWithParametersChooser(ps, factory);
    fp.getFormAccessor().replaceBean("lblmachinechoose", chooser);//$NON-NLS-1$

    rdTime = fp.getRadioButton("rdtime");//$NON-NLS-1$
    rdTime.addActionListener((e) -> updateState());

    txtseconds = (JTextField) fp.getTextComponent("txtvalue");//$NON-NLS-1$
    txtseconds.setText("2");

    rdMachine = fp.getRadioButton("machine");//$NON-NLS-1$
    rdMachine.addActionListener((e) -> updateState());

    rdgroup = new ButtonGroup();
    rdgroup.add(rdTime);
    rdgroup.add(rdMachine);

    setLayout(new BorderLayout());
    rdTime.setSelected(true);

    add(fp, BorderLayout.CENTER);
    updateState();
  }

  /** update the control state (logic between the components) */
  private void updateState() {

    SwingUtils.recurseSetEnable(this, false);
    if (rdMachine.isSelected()) {
      SwingUtils.recurseSetEnable(chooser, true);
    } else {
      SwingUtils.recurseSetEnable(txtseconds, true);
    }

    rdMachine.setEnabled(true);
    rdTime.setEnabled(true);
  }

  /**
   * create the trigger factory
   *
   * @return
   * @throws Exception
   */
  public ITriggerFactory createTriggerFactory() throws Exception {
    if (rdTime.isSelected()) {
      return new ITriggerFactory() {
        @Override
        public Trigger create(Webcam webcam, IWebCamListener listener, PerfoScanFolder psf)
            throws Exception {
          return new TimeTrigger(webcam, listener, psf, Double.parseDouble(txtseconds.getText()));
        }
      };
    }

    // default
    return new ITriggerFactory() {
      @Override
      public Trigger create(Webcam webcam, IWebCamListener listener, PerfoScanFolder psf)
          throws Exception {
        return new MachineTrigger(webcam, listener, psf, chooser.getMachineParameters());
      }
    };
  }

  /**
   * test method
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    JFrame f = new JFrame();
    f.setSize(400, 500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    f.getContentPane().setLayout(new BorderLayout());

    FilePrefsStorage p =
        new FilePrefsStorage(new File("c:\\temp\\testTriggerComponents.properties")); //$NON-NLS-1$
    p.load();
    f.getContentPane().add(new JTriggerComponent(p, new IExtension[] {new MachineExtension()}), BorderLayout.CENTER);
    f.setVisible(true);
  }
}
