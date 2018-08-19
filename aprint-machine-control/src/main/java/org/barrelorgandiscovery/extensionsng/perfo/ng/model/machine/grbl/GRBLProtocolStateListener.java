package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

public interface GRBLProtocolStateListener {

	public void statusReceived(GRBLStatus status);
	
	public void commandAck();
	
	public void unknownReceived(String line);
	
	public void resetted();
	
}
