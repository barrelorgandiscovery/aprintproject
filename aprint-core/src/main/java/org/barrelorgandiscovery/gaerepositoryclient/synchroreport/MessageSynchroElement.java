package org.barrelorgandiscovery.gaerepositoryclient.synchroreport;

public class MessageSynchroElement extends SynchroElement {

	private String message;
	private int status = SynchroElement.MESSAGE;

	public MessageSynchroElement(String message) {

		assert message != null;
		assert !"".equals(message);
		this.message = message;
	}

	public MessageSynchroElement(int status, String message) {
		this(message);
		this.status = status;
	}

	@Override
	public SynchroAction[] getAssociatedActions() {
		return new SynchroAction[0];
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public int getStatus() {
		return this.status;
	}

}
