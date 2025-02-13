package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

public interface MachineControlListener {

	/**
	 * report an error
	 * 
	 * @param message
	 */
	public void error(String message);

	/**
	 * report a status changed
	 * 
	 * @param status
	 */
	public void statusChanged(MachineStatus status);

	/**
	 * report the current machine position
	 * 
	 * @param wx workx
	 * @param wy worky
	 * @param mx machine x
	 * @param my machine y
	 */
	public void currentMachinePosition(String status, double mx, double my);

	/**
	 * report the sent command
	 * 
	 * @param commandSent
	 */
	public void rawElementSent(String commandSent);

	/**
	 * report the raw command received
	 * 
	 * @param commandReceived
	 */
	public void rawElementReceived(String commandReceived);
	
	/**
	 * 
	 * @param commands
	 */
	public void informationReceived(String commands);
	

}
