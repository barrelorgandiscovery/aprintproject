package org.barrelorgandiscovery.playsubsystem;

import org.barrelorgandiscovery.listeningconverter.MIDIListeningConverter;

/**
 * Interface for specifying the current virtualbook to midi converter,
 * it is used when the output is a midi device and not a synthetizer that produce the sound
 * 
 * @author Freydiere Patrice
 * 
 */
public interface NeedMidiListeningConverter {

	boolean isSupportMidiListeningConverter();
	void setCurrentMidiListeningConverter(MIDIListeningConverter converter);

}
