package org.barrelorgandiscovery.virtualbook.transformation.importer;

public class MidiGenericEvent extends MidiAdvancedEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5306939275848430821L;

	public MidiGenericEvent(long timestamp, byte[] datas) {
		super(timestamp);
		this.data = datas;

	}

	private byte[] data;

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public void visit(AbstractMidiEventVisitor visitor) throws Exception  {
		visitor.visit(this);
	}

}
