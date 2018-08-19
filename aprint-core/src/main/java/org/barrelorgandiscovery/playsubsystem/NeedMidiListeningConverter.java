package org.barrelorgandiscovery.playsubsystem;

import org.barrelorgandiscovery.listeningconverter.MIDIListeningConverter;

/**
 * Interface for specifying the current virtualbook to midi converter
 * 
 * @author Freydiere Patrice
 * 
 */
public interface NeedMidiListeningConverter {

	boolean isSupportMidiListeningConverter();
	void setCurrentMidiListeningConverter(MIDIListeningConverter converter);

}
