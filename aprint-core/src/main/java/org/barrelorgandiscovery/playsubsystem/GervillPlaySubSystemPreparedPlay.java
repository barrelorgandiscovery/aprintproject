package org.barrelorgandiscovery.playsubsystem;

import javax.sound.midi.Sequence;

import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.playsubsystem.prepared.IPreparedPlaying;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class GervillPlaySubSystemPreparedPlay extends Object implements
		IPreparedPlaying {

	Sequence sequenceToPlay;
	Instrument instrument;
	VirtualBook vbToPlay;

}
