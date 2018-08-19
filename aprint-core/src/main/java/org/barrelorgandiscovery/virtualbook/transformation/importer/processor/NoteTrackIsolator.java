package org.barrelorgandiscovery.virtualbook.transformation.importer.processor;

import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiAdvancedEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiNote;

public class NoteTrackIsolator implements MidiEventIsolator {

	private int track;
	
	public NoteTrackIsolator(int track)
	{
		this.track = track;
	}
	
	public boolean shouldIsole(MidiAdvancedEvent note) {
		
		if (note instanceof MidiNote) {
			MidiNote mn = (MidiNote) note;
			if (mn.getTrack() == track)
				return true;
		}
		
		return false;
	}

}
