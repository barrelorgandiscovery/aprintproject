package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import javax.swing.JPanel;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

/**
 * GRBL machine that communicate with com port
 * 
 * @author pfreydiere
 * 
 */
public class GRBLMachineParameters extends AbstractMachineParameters {

	private static final String COM_PORT_STORAGEPROPERTY_SAVE = "comPort";
	/**
	 * communication com port
	 */
	String comPort = "COM3";

	public String getPort() {
		return comPort;
	}
	
	public void setComPort(String comPort) {
		this.comPort = comPort;
	}

	

	/*
	 * (non-Javadoc)
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters#getLabelName()
	 */
	@Override
	public String getLabelName() {
		return "Machine GRBL";
	}

	@Override
	public void loadParameters(IPrefsStorage ps) throws Exception {
		assert ps != null;
		String dv = ps.getStringProperty(COM_PORT_STORAGEPROPERTY_SAVE, null);
		if (dv != null)
		{
			comPort = dv;
		}
	}
	
	@Override
	public void saveParameters(IPrefsStorage ps) throws Exception {
		assert ps != null;
		ps.setStringProperty(COM_PORT_STORAGEPROPERTY_SAVE, comPort);
	}
	
}
