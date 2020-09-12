package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;

public interface MachineControl {

	/**
	 * define the listener for machine control
	 * 
	 * @param listener
	 */
	public void setMachineControlListener(MachineControlListener listener);

	/**
	 * send a command
	 * 
	 * @param command
	 * @throws Exception
	 */
	public void sendCommand(Command command) throws Exception;

	/**
	 * prepare , and presend some informations, before sending work commands
	 * 
	 * @throws Exception
	 */
	public void prepareForWork() throws Exception;

	/**
	 * ending the work, send commands for shutting down some functionnalities
	 * 
	 * @throws Exception
	 */
	public void endingForWork() throws Exception;

	/**
	 * close the machine control
	 */
	public void close() throws Exception;

	/**
	 * wait for all commands executed
	 * 
	 * @throws Exception
	 */
	public void flushCommands() throws Exception;

	/**
	 * reset the machine processor, and clear all commands
	 * 
	 * @throws Exception
	 */
	public void reset() throws Exception;
	
	/**
	 * return the machine current status
	 * @return
	 */
	public MachineStatus getStatus();

}
