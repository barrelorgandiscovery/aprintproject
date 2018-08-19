package org.barrelorgandiscovery.virtualbook.transformation.importer;

public class MidiMetaGenericEvent extends MidiGenericEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9112555771328185532L;
	
	private int type;
	
	public MidiMetaGenericEvent(long timestamp, int type, byte[] datas) {
		super(timestamp, datas);
		this.type = type;
	}

	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "metaevent :" + type + " at " + timestamp;
	}
	
}
