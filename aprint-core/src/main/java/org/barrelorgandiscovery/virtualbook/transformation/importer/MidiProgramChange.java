package org.barrelorgandiscovery.virtualbook.transformation.importer;

public class MidiProgramChange extends MidiAdvancedEvent {

	private int channel;
	private int program;

	public MidiProgramChange(long timestamp, int channel, int program) {
		super(timestamp);
		this.channel = channel;
		this.program = program;
	}

	public int getProgram() {
		return program;
	}

	public void setProgram(int program) {
		this.program = program;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	@Override
	public void visit(AbstractMidiEventVisitor visitor) throws Exception {
		visitor.visit(this);
	}

}
