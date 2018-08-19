package org.barrelorgandiscovery.virtualbook.transformation.importer;

public class MidiControlChange extends MidiAdvancedEvent {

	private int channel;
	private int control;
	private int value;

	public MidiControlChange(long timestamp, int channel, int control,
			int value) {
		super(timestamp);
		this.channel = channel;
		this.control = control;
		this.value = value;
	}

	public int getChannel() {
		return channel;
	}

	public int getControl() {
		return control;
	}

	public int getValue() {
		return value;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}
	
	public void setControl(int control) {
		this.control = control;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public void visit(AbstractMidiEventVisitor visitor) throws Exception {
		visitor.visit(this);
	}

}
