package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionManager;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.GUIMachineParametersRepository;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLLazerMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLPunchMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.lazer.mock.MockLazerMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.mock.MockMachineParameters;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.prefs.PrefixedNamePrefsStorage;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.SwingUtils;

import com.jeta.forms.components.panel.FormPanel;

public class JMachineWithParametersChooser extends JPanel {

	/** */
	private static final long serialVersionUID = 8767588173606262592L;

	private static final String STORAGE_MACHINE_PROPERTIES_DOMAIN = "punchextension.machines."; //$NON-NLS-1$

	private static final Logger logger = Logger.getLogger(JMachineWithParametersChooser.class);

	private IPrefsStorage preferences;

	// by default
	private AbstractMachineParameters selectedMachineParameters = new GRBLPunchMachineParameters();

	private IExtension[] extensions;

	public JMachineWithParametersChooser(IPrefsStorage ps, IExtension[] extensions) throws Exception {
		this.preferences = ps;

		assert extensions != null;
		this.extensions = extensions;

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

		String machineLabelDomain = StringTools.toHex(gp.getLabelName());

		PrefixedNamePrefsStorage pps = new PrefixedNamePrefsStorage(
				STORAGE_MACHINE_PROPERTIES_DOMAIN + machineLabelDomain, preferences);
		return pps;
	}

	protected void initComponents() throws Exception {

		InputStream is = getClass().getResourceAsStream("machinechoosecomponent.jfrm"); //$NON-NLS-1$
		assert is != null;
		FormPanel fp = new FormPanel(is);
		add(fp, BorderLayout.CENTER);

		GRBLPunchMachineParameters grblpunchpachineparameters = new GRBLPunchMachineParameters();
		try {
			PrefixedNamePrefsStorage pps = constructMachinePreferenceStorage(grblpunchpachineparameters);
			grblpunchpachineparameters.loadParameters(pps);
		} catch (Exception ex) {
			logger.error("error while loading the preference storage for machine :" //$NON-NLS-1$
					+ ex.getMessage(), ex);
		}

		// default selected
		selectedMachineParameters = grblpunchpachineparameters;
		
		MachineParameterFactory factory = new MachineParameterFactory(extensions);

		AbstractMachineParameters[] allDiscoveredParameters = factory.createAllMachineParameters();
		
		MachineParameterDisplayer[] displayers = Arrays.stream(allDiscoveredParameters)
				.map( (p) -> new MachineParameterDisplayer(p))
				.collect(Collectors.toList()).toArray(new MachineParameterDisplayer[0]);

		machineCombo = fp.getComboBox("cbmachine"); //$NON-NLS-1$
		machineCombo.setModel(new DefaultComboBoxModel<MachineParameterDisplayer>(displayers));
		machineCombo.addItemListener(i -> {
			selectedMachineParameters = ((MachineParameterDisplayer) i.getItem()).mp;
		});

		JButton parametersChoice = (JButton) fp.getButton("btnparameters"); //$NON-NLS-1$
		parametersChoice.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					// display machine parameters

					GUIMachineParametersRepository guiMachineParametersGUIRepository = new GUIMachineParametersRepository();

					JPanel panelParameter = guiMachineParametersGUIRepository
							.createMachineParameters(selectedMachineParameters);
					
					if (panelParameter == null) {
						JOptionPane.showMessageDialog(JMachineWithParametersChooser.this,"No Parameters for this machine");
						return;
					}

					JDialog dialog = new JDialog();
					Container cp = dialog.getContentPane();
					cp.setLayout(new BorderLayout());
					cp.add(panelParameter, BorderLayout.CENTER);
					dialog.setSize(500, 300);
					SwingUtils.center(dialog);

					dialog.setVisible(true);

				} catch (Exception ex) {
					logger.error("error while opening the machine parameter settings :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
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

		f.getContentPane().add(new JMachineWithParametersChooser(p, new IExtension[0]), BorderLayout.CENTER);
		f.setVisible(true);
	}
}
