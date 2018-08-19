package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.util.ArrayList;

/**
 * Bag class for getting the midi read error associated to a midi file
 * @author use
 *
 */
public class MidiFileReadResult {

	public ArrayList<MidiFileReadError> errors = null;
	
	public MidiFile midiFile;
	
}
