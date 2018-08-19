package org.barrelorgandiscovery.virtualbook.transformation.importer;

public class MidiTempoEvent extends MidiAdvancedEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5000395479487572299L;

	private long beatLength;

	public MidiTempoEvent(long timestamp, long beatLength) {
		super(timestamp);
		this.beatLength = beatLength;
	}

	/**
	 * Define the beat length in microsecond
	 * 
	 * @param beatLength
	 */
	public void setBeatLength(long beatLength) {
		this.beatLength = beatLength;
	}

	/**
	 * get the beat length in microsecond
	 * 
	 * @return
	 */
	public long getBeatLength() {
		return beatLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.virtualbook.transformation.importer.
	 * MidiAdvancedEvent
	 * #visit(org.barrelorgandiscovery.virtualbook.transformation
	 * .importer.AbstractMidiEventVisitor)
	 */
	@Override
	public void visit(AbstractMidiEventVisitor visitor) throws Exception {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TempoChanged ts : ").append(this.timestamp);
		sb.append(" beatLength :").append(this.beatLength).append(" micros");
		return sb.toString();
	}

}
