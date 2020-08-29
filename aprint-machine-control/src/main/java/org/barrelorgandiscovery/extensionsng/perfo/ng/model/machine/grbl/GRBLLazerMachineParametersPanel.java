package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jssc.SerialPortList;

import org.apache.log4j.Logger;

import com.jeta.forms.components.panel.FormPanel;

/**
 * panel for selecting the com port
 * 
 * @author pfreydiere
 * 
 */
public class GRBLLazerMachineParametersPanel extends JPanel {

	private static Logger logger = Logger
			.getLogger(GRBLLazerMachineParametersPanel.class);

	/**
	 * associated parameters
	 */
	private GRBLLazerMachineParameters parameters;

	public GRBLLazerMachineParametersPanel(GRBLLazerMachineParameters parameters)
			throws Exception {
		assert parameters != null;
		this.parameters = parameters;
		initComponents();
	}

	protected void initComponents() throws Exception {
		// list serial ports

		String[] portNames = SerialPortList.getPortNames();

		FormPanel f = new FormPanel(getClass().getResourceAsStream(
				"grblserialandparameters.jfrm")); //$NON-NLS-1$

		JComboBox<String> cb = (JComboBox<String>) f
				.getComponentByName("cbportserie"); //$NON-NLS-1$
		cb.setModel(new DefaultComboBoxModel<>(portNames));

		// define the default selected element 
		cb.setSelectedItem(parameters.comPort);
		
		String it = (String)cb.getSelectedItem();
		if (it != null) 
		{
			logger.debug("define default com port"); //$NON-NLS-1$
			parameters.comPort = it;
		}
		
		cb.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					
					parameters.comPort = (String) e.getItem();
					logger.debug("new port selected :" + parameters.comPort); //$NON-NLS-1$
				}
			}
		});
		
		
		JSpinner maxpower = (JSpinner)f.getComponentByName("maxPowerValue");//$NON-NLS-1$
		maxpower.setModel(new SpinnerNumberModel((int)parameters.getMaxPower(), 100, 10_000, 10));
		maxpower.getModel().setValue(parameters.getMaxPower());
		maxpower.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					parameters.setMaxPower((Integer)maxpower.getValue());
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
				
			}
		});

		
		
		JSpinner maxspeed = (JSpinner)f.getComponentByName("maxSpeedValueSpin");//$NON-NLS-1$
		maxspeed.setModel(new SpinnerNumberModel((int)parameters.getMaxspeed(), 100, 10_000, 10));
		maxspeed.getModel().setValue(parameters.getMaxspeed());
		maxspeed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					parameters.setMaxspeed((Integer)maxspeed.getValue());
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
				
			}
		});
		
		setLayout(new BorderLayout());
		add(f, BorderLayout.CENTER);
	}

}
