package groovy.aprint.midi

import groovy.aprint.transform.Drum;
import groovy.aprint.transform.Note
import groovy.aprint.transform.ScaleHelper
import groovy.aprint.transform.Track

import org.barrelorgandiscovery.scale.Scale
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.importer.*

/**
 * Helper class for transforming the midi files, by track associations
 * 
 * @author pfreydiere
 *
 */
class MidiTransformHelper {

	/**
	 * The associated scale helper
	 */
	private ScaleHelper s;

	/**
	 * The group to transform with this transformation
	 */
	private MidiEventGroup gForTransform;

	/**
	 * The current midi transform
	 */
	private LinearMidiImporter currentTransformImporter;

	/**
	 * Associated transform to merge for the result
	 */
	private List<MidiTransformHelper> linkedTransforms =
	new ArrayList<MidiTransformHelper>()

	/**
	 * Constructor with scale and midi event group
	 * @param s
	 * @param eg
	 */
	MidiTransformHelper(Scale s, MidiEventGroup eg) {
		this.s = new ScaleHelper(s);
		this.gForTransform = eg;
		currentTransformImporter = new LinearMidiImporter(s);
	}

	/**
	 * Constructor with the midi helper and event group
	 * @param sh
	 * @param eg
	 */
	MidiTransformHelper(ScaleHelper sh, MidiEventGroup eg) {
		this.s = sh;
		this.gForTransform = eg;
		currentTransformImporter = new LinearMidiImporter(s.scale);
	}

	/**
	 * Define association between notes from the midi file, into tracks associated to the scale
	 * @param notes
	 * @param tracks
	 * @return
	 */
	MidiTransformHelper map(Note[] notes , Track[] tracks) {
		if (notes == null || tracks == null)
			throw new Exception("null parameter passed");

		if (notes.length != tracks.length)
			throw new Exception("notes array and tracks array must have the same length")

		notes.eachWithIndex { e,i ->
			map(e, tracks[i]);
		}

		return this
	}

	/**
	 * Map a midi not with the track passed in parameter, if one parameter is null
	 * an exception is raised.
	 * 
	 * @param n the note
	 * @param t the track to associate to
	 * @return the current element for cascading operations
	 */
	MidiTransformHelper map(Note n, Track t) {

		if (n == null || t == null)
			throw new Exception("null argument passed");

		currentTransformImporter.mapNote(n.midiCode, t.no)

		return this
	}
	
	/**
	* Map a midi note not in channel 9, but handled for percussions, if one parameter is null
	* an exception is raised.
	*
	* @param n the note
	* @param t the track to associate to
	* @return the current element for cascading operations
	*/
   MidiTransformHelper mapDrum(Note n, Track t) {

	   if (n == null || t == null)
		   throw new Exception("null argument passed");
		   
	   if (!t.isDrum())
	       throw new Exception("track defined is not a drum track");
		   
	   currentTransformImporter.mapNoteForPercussion(n.midiCode, t.no)

	   return this
   }
	
	/**
	 * Map a drum on a track, the drum must be on canal 10 (9)
	 * the mapped drum associate the discharge effect on the book
	 * @param d the drum definition
	 * @param t
	 * @return
	 */
	MidiTransformHelper map(Drum d, Track t) {
		if (d == null || t == null)
			throw new Exception("null argument passed");
		
		currentTransformImporter.mapPercussion(d.midiCode, t.no)
			
		return this
	}
	

	/**
	 * 
	 * @param assoc, a hash containing a "notes" key associated with an array of notes, 
	 * and a "tracks" keys associated to an array of track
	 * @return
	 */
	MidiTransformHelper map(Map assoc) {
		
		if (!(assoc["notes"])) {
			throw new Exception("hash must contain a \"notes\" key")
		}
		if (!(assoc["tracks"])) {
			throw new Exception("hash must contain a \"tracks\" key")
		}
		
		map( assoc["notes"] as Note[], assoc["tracks"] as Track[])
		
	}


	/**
	 * Launch the midi transform ...
	 * 
	 * @return the conversion result, with errors
	 */
	MidiConversionResult doConvert() {

		MidiConversionResult r = currentTransformImporter.convert(gForTransform)

		linkedTransforms.each {
			MidiConversionResult r2 = it.doConvert();

			// merging result

			VirtualBook v = r.virtualbook

			r2.virtualbook.getOrderedHolesCopy().each {
				r.virtualbook.addHole(it);
			}

			// Add events
			r2.virtualbook.getOrderedEventsByRef().each {
				r.virtualbook.addEvent(it);
			}
			

			// preserve issues
			if (r.issues == null)
				r.issues = new ArrayList<MidiConversionProblem>();

			if (r2.issues != null)
			{
				r2.issues.each {
					r.issues.add(it)
				}
			}

		}

		// return the result
		r
	}

	/**
	 * Merge the transformation with the transform passed in parameter
	 * @param t
	 * @return the current transformation
	 */
	MidiTransformHelper mergeWith(MidiTransformHelper t) {

		if (t == null) {
			throw new Exception("null transformation passed")
		}

		// append to the linked transforms
		linkedTransforms << t

		return this
	}
}
