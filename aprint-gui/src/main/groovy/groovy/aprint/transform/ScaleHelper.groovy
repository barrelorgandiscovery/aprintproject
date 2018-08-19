
package groovy.aprint.transform

import org.barrelorgandiscovery.scale.*
import org.barrelorgandiscovery.instrument.*
import org.barrelorgandiscovery.tools.*

/**
 * Class helper for finding tracks in the scale / instrument 
 */
class ScaleHelper {

	Scale scale

	String BASS =  PipeStopListReference.REGISTERSET_BASSE
	String ACCOMPAGNEMENT =  PipeStopListReference.REGISTERSET_ACCOMPAGNEMENT
	String ACCOMPAGNMENT =  PipeStopListReference.REGISTERSET_ACCOMPAGNEMENT
	String MELODY =  PipeStopListReference.REGISTERSET_CHANT
	String COUNTER_MELODY = PipeStopListReference.REGISTERSET_CONTRECHAMP
	String MELODY3 =  PipeStopListReference.REGISTERSET_CHANT3

	def registerSets = ["bass" : BASS, "accompagnement" : ACCOMPAGNEMENT,"accompagnment" : ACCOMPAGNEMENT, "melody" : MELODY,
		"counterMelody" : COUNTER_MELODY, "melody3" : MELODY3]

	public ScaleHelper(Instrument ins,Map m=null) {
		m?.each { k,v ->
			this."$k" = m
		}
		scale = ins.scale;
	}

	public ScaleHelper(Scale s, Map m=null) {
		m?.each { k,v ->
			this."$k" = m
		}
		scale = s
	}

	public ScaleHelper(Map m) {
		if (m["scale"] == null)
			throw new Exception("scale attribute must be defined");
		scale = m["scale"]
	}

	private def tks() {
		Map m = [:]
		scale.tracksDefinition.eachWithIndex { it , index -> m[index]=it }
		m
	}

	// methods to get subset of the scale (notes, tracks and the region name)
	def propertyMissing(String name) {

		if (name in registerSets.keySet())
		{
			String rn = registerSets[name]
			Note[] n =  notes(rn)
			return ["notes" : n,"tracks" : tracks(n, rn), "name" : rn]
		}

		throw new MissingPropertyException(name, getClass());

	}

	/** 
	 * find a track associated to a note (the note is given in parameter)
	 * the search is done wheter the note is associated to a specific RegisterSet (BASS, ACCOMPAGNMENT, MELODY ...)
	 * if the Track is not found, the method return null
	 * 
	 * @param note the note represented as String with octave eg : "D5"
	 * @return the found track or NULL 
	 * 
	 */
	Track find(String note) {
		def n = MidiHelper.midiCode(note)
		def found = tks().find { it.value?.hasProperty("midiNote") ? it.value.midiNote == n : false }
		return found == null ? null : new Track(s : scale, no : found.key)
	}

	/////////////////////////////////////////////////////////
	// track *
	
	/**
	 * get a track object from its index
	 */
	Track track(int t) { new Track(no:t, s:scale) }

	/** 
	 * get a track associated to a note, independently of the registerset
	 */
	Track track(Note n) {
		return find(MidiHelper.midiLibelle(n.midiCode))
	}

	/**
	* get a note track in a specific register set, if the registerset is null, the method returns the first note encountered
	* @parameter n the note to find
	* @parameter registerSet the registerset in which finding the track, if null it ignore the registerset
	* @return null if the track hasn't been found
	*/
   Track track(Note n, String registerSet)
   {
	   def foundindex = tks().find {
		   it.value.hasProperty("midiNote") ?
				   it.value.midiNote == n.midiCode && (registerSet == it.value.registerSetName || registerSet == null) :
				   false }
	   return foundindex == null ? null : new Track(s : scale, no : foundindex.key)
   }

	
	/**
	 * get Tracks
	 */
	Track[] getTracks() {
		(0..scale.tracksDefinition.length - 1).collect { new Track(no:it, s:this.scale) }
	}

	/**
	 * get Notes
	 */
	Note[] getNotes() {
		notes(null)	
	}
	
	

	/**
	 * Find a track with this not (independently of the octave)
	 * @param n the note
	 * @return the track, or null if not found
	 */
	Track trackWithoutOctave(Note n)
	{
		trackWithoutOctave(n,null)
	}
	
	/**
	 * find a track in a register set independently of the octave
	 */
	Track trackWithoutOctave(Note n, String registerSet)
	{
		def foundindex = tks().find {
			it.value.hasProperty("midiNote") ?
					MidiHelper.getMidiNote(it.value.midiNote) == MidiHelper.getMidiNote(n.midiCode) && (registerSet == it.value.registerSetName || registerSet == null) :
					false }
		return foundindex == null ? null : new Track(s : scale, no : foundindex.key)
	}
	
	/**
	 * get all the tracks to notes without octave, when the note is not found, a null is returned in
	 * the array
	 * @parameter notes an array containing the notes to find
	 */
	Track[] tracksWithoutOctave(Note[] notes, String registerSet)
	{
		notes.collect { trackWithoutOctave(it, registerSet) }
	}

	/**
	 * Get All tracks associated to given notes, without octave match
	 * @param notes
	 * @return
	 */
	Track[] tracksWithoutOctave(Note[] notes)
	{
		tracksWithoutOctave(notes, null)
	}
	
	
	/**
	 * get all the notes of a registerset
	 *
	 */
	Note[] notes(String registerSet)
	{
		def n = tks().findAll{ it ->
			it.value.hasProperty("midiNote") ?
					(registerSet == null || registerSet == it.value.registerSetName) :
					false }
		n.collect { new Note(midiCode : it.value.midiNote) }
	}

	/**
	 * get all the tracks associated to notes in a registerset
	 * nota : if the registerset is null, the search is done in all registersets
	 * @return the track list, containing nulls if track is not found
	 */ 
	Track[] tracks(Note[] notes, String registerSet)
	{
		notes.collect { track((Note)it, registerSet) }
	}

	/**
	 * get all the tracks associated to notes in all registerset
	 */
	Track[] tracks(Note[] notes)
	{
		tracks(notes, null)
	}
	

	/**
	 * get all the tracks containing a note definition
	 */
	Track[] getNoteTracks() {
		def l = tks().findAll { it.value instanceof NoteDef }
		return l.collect { new Track(s:scale, no:it.key) }
	}

	/**
	 * get all the tracks containing registers definitions
	 */
	Track[] getRegisterTracks() {
		def l = tks().findAll { it.value instanceof AbstractRegisterCommandDef }
		return l.collect { new Track(s:scale, no:it.key) }
	}

	/**
	 * get all the tracks containing a percussion (drum) definition
	 */
	Track[] getPercussionTracks() {
		def l = tks().findAll { it.value instanceof PercussionDef }
		return l.collect { new Track(s:scale, no:it.key) }
	}

	/**
	 * alias for getPercussionTracks, for english people
	 * @return
	 */
	Track[] getDrumTracks()
	{
		getPercussionTracks()
	}


	/**
	 *
	 * Create a transform helper to the scale given in parameter
	 *
	 */
	TransformHelper transformFor(Scale s)
	{
		new TransformHelper(this.scale, s)
	}

	/**
	 * Create a transform helper to the scale helper given in parameter
	 */
	TransformHelper transformFor(ScaleHelper s)
	{
		new TransformHelper(this, s)
	}

	/**
	 * Get the underlying scale
	 * @return the scale
	 */
	Scale getScale()
	{
		return this.scale;
	}


}
