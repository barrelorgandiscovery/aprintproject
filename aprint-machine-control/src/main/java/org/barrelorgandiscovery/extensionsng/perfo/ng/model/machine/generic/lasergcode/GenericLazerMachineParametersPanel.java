package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.generic.lasergcode;

import java.awt.BorderLayout;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;

import com.jeta.forms.components.panel.FormPanel;

/**
 * panel for selecting the com port
 * 
 * @author pfreydiere
 * 
 */
public class GenericLazerMachineParametersPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4410438588377866867L;

	private static Logger logger = Logger.getLogger(GenericLazerMachineParametersPanel.class);

	/**
	 * associated parameters
	 */
	private GenericLazerMachineParameters parameters;

	/**
	 * constructor,
	 * 
	 * @param parameters passed by reference
	 * @throws Exception
	 */
	public GenericLazerMachineParametersPanel(GenericLazerMachineParameters parameters) throws Exception {
		assert parameters != null;
		this.parameters = parameters;
		initComponents();
	}

	private void registerChange(JTextField field, Consumer<JTextField> f) {
		field.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				f.accept(field);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				f.accept(field);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				f.accept(field);
			}
		});
	}

	protected void initComponents() throws Exception {
		// list serial ports

		FormPanel f = new FormPanel(getClass().getResourceAsStream("gcodeparameters.jfrm")); //$NON-NLS-1$

		f.getLabel("lblmaxSpeedValue") //$NON-NLS-1$
				.setText(Messages.getString("GRBLLazerMachineParametersPanel.0")); //$NON-NLS-1$
		f.getLabel("lblMaxPower") //$NON-NLS-1$
				.setText(Messages.getString("GRBLLazerMachineParametersPanel.1")); //$NON-NLS-1$

		JSpinner maxpower = (JSpinner) f.getComponentByName("maxPowerValue");//$NON-NLS-1$
		maxpower.setModel(new SpinnerNumberModel((int) parameters.getMaxPower(), 5, 3000, 5));
		maxpower.getModel().setValue(parameters.getMaxPower());
		maxpower.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					parameters.setMaxPower((Integer) maxpower.getValue());
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		});

		JSpinner maxspeed = (JSpinner) f.getComponentByName("maxSpeedValueSpin");//$NON-NLS-1$
		maxspeed.setModel(new SpinnerNumberModel((int) parameters.getMaxspeed(), 100, 10_000, 10));
		maxspeed.getModel().setValue(parameters.getMaxspeed());
		maxspeed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					parameters.setMaxspeed((Integer) maxspeed.getValue());
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}

			}
		});

		// label translation
		f.getLabel("lblendBookPrecommands") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.0")); //$NON-NLS-1$

		JTextField endBookPrecommands = (JTextField) f.getComponentByName("endBookPrecommands");//$NON-NLS-1$
		endBookPrecommands.setText(parameters.getEndBookPrecommands());
		registerChange(endBookPrecommands, (e) -> {
			parameters.setEndBookPrecommands(e.getText());
		});

		f.getLabel("lblhomingCommands") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.1")); //$NON-NLS-1$

		JTextField homingCommands = (JTextField) f.getComponentByName("homingCommands");//$NON-NLS-1$
		homingCommands.setText(parameters.getHomingCommands());
		registerChange(homingCommands, (e) -> {
			parameters.setHomingCommands(e.getText());
		});

		f.getLabel("lblstartBookPrecommands") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.2")); //$NON-NLS-1$

		
		JTextField startBookPrecommands = (JTextField) f.getComponentByName("startBookPrecommands");//$NON-NLS-1$
		startBookPrecommands.setText(parameters.getStartBookPrecommands());
		registerChange(startBookPrecommands, (e) -> {
			parameters.setStartBookPrecommands(e.getText());
		});
		
		f.getLabel("lbldisplacementPreCommand") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.3")); //$NON-NLS-1$


		JTextField displacementPreCommand = (JTextField) f.getComponentByName("displacementPreCommand");
		displacementPreCommand.setText(parameters.getDisplacementPreCommand());
		registerChange(displacementPreCommand, (e) -> {
			parameters.setDisplacementPreCommand(e.getText());
		});
		
		f.getLabel("lbldisplacementCommandPattern") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.4")); //$NON-NLS-1$

		JTextField displacementCommandPattern = (JTextField) f.getComponentByName("displacementCommandPattern");
		displacementCommandPattern.setText(parameters.getDisplacementCommandPattern());
		registerChange(displacementCommandPattern, (e) -> {
			parameters.setDisplacementCommandPattern(e.getText());
		});
		f.getLabel("lbldisplacementPostCommand") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.5")); //$NON-NLS-1$

		JTextField displacementPostCommand = (JTextField) f.getComponentByName("displacementPostCommand");
		displacementPostCommand.setText(parameters.getDisplacementPostCommand());
		registerChange(displacementPostCommand, (e) -> {
			parameters.setDisplacementPostCommand(e.getText());
		});
		
		f.getLabel("lblcuttingPreCommand") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.6")); //$NON-NLS-1$

		JTextField cuttingPreCommand = (JTextField) f.getComponentByName("cuttingPreCommand");
		cuttingPreCommand.setText(parameters.getCuttingPreCommand());
		registerChange(cuttingPreCommand, (e) -> {
			parameters.setCuttingPreCommand(e.getText());
		});
		f.getLabel("lblcuttingToCommandPattern") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.7")); //$NON-NLS-1$

		JTextField cuttingToCommandPattern = (JTextField) f.getComponentByName("cuttingToCommandPattern");
		cuttingToCommandPattern.setText(parameters.getCuttingToCommandPattern());
		registerChange(cuttingToCommandPattern, (e) -> {
			parameters.setCuttingToCommandPattern(e.getText());
		});
		
		f.getLabel("lblcuttingPostCommand") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.8")); //$NON-NLS-1$

		JTextField cuttingPostCommand = (JTextField) f.getComponentByName("cuttingPostCommand");
		cuttingPostCommand.setText(parameters.getCuttingPostCommand());
		registerChange(cuttingPostCommand, (e) -> {
			parameters.setCuttingPostCommand(e.getText());
		});
		
		f.getLabel("lblpowerChangeCommand") //$NON-NLS-1$
		.setText(Messages.getString("GenericLazerMachineParametersPanel.9")); //$NON-NLS-1$

		JTextField powerChangeCommand = (JTextField) f.getComponentByName("powerChangeCommand");
		powerChangeCommand.setText(parameters.getPowerChangeCommand());
		registerChange(powerChangeCommand, (e) -> {
			parameters.setPowerChangeCommand(e.getText());
		});

		setLayout(new BorderLayout());

		add(f, BorderLayout.CENTER);
	}

}
