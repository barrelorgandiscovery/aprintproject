package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

public interface MachineDirectControl extends MachineControl{

	public void directCommand(String command) throws Exception;

}
