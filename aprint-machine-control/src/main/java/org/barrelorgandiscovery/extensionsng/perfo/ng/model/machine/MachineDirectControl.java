package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

/**
 * this is an extension of machine control, ability for the machine to send a
 * direct command (for control)
 * 
 * @author pfreydiere
 *
 */
public interface MachineDirectControl extends MachineControl {

	/**
	 * send a direct command
	 * 
	 * @param command
	 * @throws Exception
	 */
	public void directCommand(String command) throws Exception;

}
