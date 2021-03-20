package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

/**
 * state listener interface
 * 
 * @author pfreydiere
 *
 */
public interface GRBLProtocolStateListener {

	public void statusReceived(GRBLStatus status);

	public void commandAck();

	public void unknownReceived(String line);

	public void resetted();
	
	public void welcome(String welcomeString);

}
