package groovy.aprint.midi

import groovy.aprint.transform.Note;
import groovy.aprint.transform.ScaleHelper;

import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiAdvancedEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiConversionResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiEventGroup;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiNote

/**
 * Helper Category for midi handling
 * 
 * @author pfreydiere
 *
 */
@Category(MidiEventGroup)
class MidiCategory {

	/**
	 * Filter event in a channel and return only the events link to the channel passed in parameter
	 * @param channel
	 * @return
	 */
	MidiEventGroup filterChannel(int channel) {
		MidiEventGroup g = new MidiEventGroup();
		g.addAll(this.findAll { it.hasProperty("channel") && it.channel == channel })
		g
	}

	/**
	 * Filter events from a specific track, and return only the events in this track
	 * @param trackNo
	 * @return
	 */
	MidiEventGroup filterMidiTrack(int trackNo) {
		MidiEventGroup g = new MidiEventGroup();
		g.addAll(this.findAll { it.hasProperty("track") && it.track == trackNo })
		g
	}


	/**
	 * Filter the note in a midiEvent Group
	 * @return all the note events from the collection (which can have tempo or generic midi messages)
	 */
	MidiEventGroup filterNotes() {
		MidiEventGroup g = new MidiEventGroup();
		g.addAll(this.findAll { it.hasProperty("midiNote") })
		g
	}

	/**
	* Filter the collection with a specific filter
	* @param c the closure that take the event in first parameter and return a boolean for the filter
	* @return
	*/
   MidiEventGroup filter(Closure c) {
	   MidiEventGroup g = new MidiEventGroup();
	   g.addAll(this.findAll { c(it) })
	   g
   }

  
	
	/**
	 * Return a hash with all the notes and the count of each notes
	 * the notes are the keys, the values are the cardinality
	 * 
	 * @return
	 */
	def countDistinctNotes()
	{
		def m = [:]
		this.filterNotes().each {
			
			def n = new Note(midiCode:it.midiNote)
			
			if (m[n])
			{
				m[n] ++
			} else 
			{
				m[n] = 1
			}
		}
		return m
	}
	
	

	/**
	 * List distinct notes from the midi files
	 * @return an array containing the notes used in the midi event group
	 */
	Note[] listDistinctNotes() {

		def m = countDistinctNotes()
		m.keySet().sort().toArray(new Note[0])
	}
	
	
	/**
	 * List the tracks from the event group
	 * @return
	 */
	int[] listTracks() {
		def m = [:]
		this.each {
			if (it.hasProperty("track"))
				m[it.track] = ""
		}

		def r = []
		m.each {k, v-> r<<k}

		r.sort().toArray(new int[0])
	}


	/**
	 * Create transform for Midi into VirtualBook conversion
	 * @param s
	 * @return
	 */
	MidiTransformHelper transformFor(Scale s) {
		new MidiTransformHelper(s, this)
	}


	/**
	 * Create Transform for Midi into VirtualBook conversion
	 * @param s
	 * @return
	 */
	MidiTransformHelper transformFor(ScaleHelper s) {
		new MidiTransformHelper(s, this)
	}

	/**
	 * Write the group to a midi file 0
	 * @param saveTo the file in which save the group of event
	 * @return
	 */
	def save(File saveTo) {
		MidiFile mf = new MidiFile();
		mf.addAll(this)

		MidiFileIO.write_midi_0(mf, saveTo)

		mf
	}
	
	/**
	 * Add midi note in this group
	 * @param channel the midi channel
	 * @param midiNote the midi note
	 * @param timestamp the note start in microseconds
	 * @param length the note length in microseconds
	 * @return
	 */
	def addNote(int channel, int midiNote, long timestamp, long length)
	{
		
		this << new MidiNote(timestamp, length, midiNote, 0, channel)
		
		return this;
	}
	
	
	
}
