package org.barrelorgandiscovery.virtualbook.transformation.importer;

public class MidiSignatureEvent extends MidiAdvancedEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6900805304206383716L;

	private int numerator;
	private int denominator;

	public MidiSignatureEvent(long timestamp, int numerator, int denominator) {
		super(timestamp);
		this.numerator = numerator;
		this.denominator = denominator;
	}

	public int getNumerator() {
		return numerator;
	}

	public void setNumerator(int numerator) {
		this.numerator = numerator;
	}

	public int getDenominator() {
		return denominator;
	}

	public void setDenominator(int denominator) {
		this.denominator = denominator;
	}
	
	@Override
	public void visit(AbstractMidiEventVisitor visitor) throws Exception {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MidiSignatureEvent ts : ").append(this.timestamp);
		sb.append(" signature :").append(this.numerator).append("/").append(this.denominator);
		return sb.toString();
	}
	
}
