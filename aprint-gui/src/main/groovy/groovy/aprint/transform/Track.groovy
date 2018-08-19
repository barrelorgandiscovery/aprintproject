
package groovy.aprint.transform

import org.barrelorgandiscovery.scale.*
import org.barrelorgandiscovery.tools.*

/** 
 * Track on an instrument scale, reference to a track, handling
 * the track index and remember the origin scale
 */ 
class Track {
	/*
	 * Track number
	 */
    int no 
	/*
	 * Scale associated to the track
	 */
    Scale s
	
	/**
	 * Create a track range, from the given track to the other one
	 */
    Track[] to(int other) { (this.no .. other).collect { new Track(no:it, s:s) } }
	
	/**
	 * Return if the track is a drum
	 * @return
	 */
	boolean isDrum()
	{
		return (s.getTracksDefinition()[no] instanceof PercussionDef)
	}
	
	/**
	 * return if the track is a note
	 * @return
	 */
	boolean isNote()
	{
		return (s.getTracksDefinition()[no] instanceof NoteDef)
	}
	
	
	/**
	 * Dump the object to a string
	 */
    String toString() { "Track no ${no} -> ${s.tracksDefinition[no]}" }
}