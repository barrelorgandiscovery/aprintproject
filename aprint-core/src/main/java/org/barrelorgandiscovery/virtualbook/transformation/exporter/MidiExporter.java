package org.barrelorgandiscovery.virtualbook.transformation.exporter;

import javax.sound.midi.Sequence;

import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;

public class MidiExporter {

	public Sequence exportMidi(MidiFile f) throws Exception {
		MidiExporterVisitor me = new MidiExporterVisitor();
		f.visitEvents(me);
		return me.getSequence();
	}
	
	public Sequence exportMidi(MidiFile f, int resolution) throws Exception {
		MidiExporterVisitor me = new MidiExporterVisitor(resolution);
		f.visitEvents(me);
		return me.getSequence();
	}

}
