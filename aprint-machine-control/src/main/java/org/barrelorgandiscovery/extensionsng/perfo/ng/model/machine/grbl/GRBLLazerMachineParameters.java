package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

/**
 * GRBL machine that communicate with com port
 * 
 * @author pfreydiere
 * 
 */
public class GRBLLazerMachineParameters extends AbstractMachineParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = -443494675635558228L;
	
	private static final String COM_PORT_STORAGEPROPERTY_SAVE = "comPort";
	private static final String MAX_SPEED = "maxSpeed";
	private static final String MAX_POWER = "maxPower";
	
	
	/**
	 * communication com port
	 */
	String comPort = "COM3";

	Integer maxspeed = 1000;
	Integer maxPower = 1000;

	public String getPort() {
		return comPort;
	}

	public void setComPort(String comPort) {
		this.comPort = comPort;
	}

	public Integer getMaxspeed() {
		return maxspeed;
	}

	public void setMaxspeed(Integer maxspeed) {
		assert maxspeed != null;
		assert maxspeed > 0;
		this.maxspeed = maxspeed;
	}

	public void setMaxPower(Integer maxPower) {
		this.maxPower = maxPower;
	}
	
	public Integer getMaxPower() {
		return maxPower;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.
	 * AbstractMachineParameters#getLabelName()
	 */
	@Override
	public String getLabelName() {
		return "Lazer Machine GRBL 1.1";
	}

	@Override
	public void loadParameters(IPrefsStorage ps) throws Exception {
		assert ps != null;
		String dv = ps.getStringProperty(COM_PORT_STORAGEPROPERTY_SAVE, null);
		if (dv != null) {
			comPort = dv;
		}
		
		maxspeed = ps.getIntegerProperty(MAX_SPEED, 1000);
		maxPower = ps.getIntegerProperty(MAX_POWER, 1000);
	}

	@Override
	public void saveParameters(IPrefsStorage ps) throws Exception {
		assert ps != null;
		ps.setStringProperty(COM_PORT_STORAGEPROPERTY_SAVE, comPort);
		ps.setIntegerProperty(MAX_SPEED, maxspeed);
		ps.setIntegerProperty(MAX_POWER, maxPower);
		
	}

}
