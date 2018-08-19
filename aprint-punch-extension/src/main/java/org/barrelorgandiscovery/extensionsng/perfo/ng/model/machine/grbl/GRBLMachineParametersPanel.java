package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import jssc.SerialPortList;

import org.apache.log4j.Logger;

import com.jeta.forms.components.panel.FormPanel;

/**
 * panel for selecting the com port
 * 
 * @author pfreydiere
 * 
 */
public class GRBLMachineParametersPanel extends JPanel {

	private static Logger logger = Logger
			.getLogger(GRBLMachineParametersPanel.class);

	/**
	 * associated parameters
	 */
	private GRBLMachineParameters parameters;

	public GRBLMachineParametersPanel(GRBLMachineParameters parameters)
			throws Exception {
		assert parameters != null;
		this.parameters = parameters;
		initComponents();
	}

	protected void initComponents() throws Exception {
		// list serial ports

		String[] portNames = SerialPortList.getPortNames();

		FormPanel f = new FormPanel(getClass().getResourceAsStream(
				"serialport.jfrm")); //$NON-NLS-1$

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
		
		setLayout(new BorderLayout());
		add(f, BorderLayout.CENTER);

	}

}
