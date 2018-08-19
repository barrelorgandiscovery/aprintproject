package org.barrelorgandiscovery.listeningconverter;

import javax.sound.midi.Sequence;

import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Interface for providing a VirtualBook to Midi Sequence conversion
 * 
 * @author use
 * 
 */
public interface MIDIListeningConverter {

	public abstract Sequence convert(VirtualBook vb) throws Exception;

}