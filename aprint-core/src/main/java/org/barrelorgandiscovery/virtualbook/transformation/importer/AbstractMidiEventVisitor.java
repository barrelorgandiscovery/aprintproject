package org.barrelorgandiscovery.virtualbook.transformation.importer;

/**
 * Visitor for the midi events
 * 
 * @author use
 * 
 */
public abstract class AbstractMidiEventVisitor {

	public abstract void visit(MidiNote midiNote) throws Exception;

	public abstract void visit(MidiTempoEvent midiTempoEvent) throws Exception;

	public abstract void visit(MidiSignatureEvent midiSignatureEvent)
			throws Exception;

	public abstract void visit(MidiGenericEvent midiGenericEvent)
			throws Exception;

	public abstract void visit(MidiControlChange midiControlChangeEvent)
			throws Exception;

	public abstract void visit(MidiProgramChange midiProgramChange)
			throws Exception;

}
