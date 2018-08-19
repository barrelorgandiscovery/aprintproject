
package groovy.aprint.transform

import org.barrelorgandiscovery.scale.*
import org.barrelorgandiscovery.tools.MidiHelper

/**
 * This Category permit to transform a string in Note object
 * 
 * example :  
 * 
 *     "D4".note
 * 
 * @author pfreydiere
 *
 */
@Category(String)
class NoteCategory {
    
	/**
	 * Get the note from a String scription, 
	 * the string is formatted as this : "C3"
	 * @return
	 */
    Note getNote() { 
		try {
			new Note(midiCode : MidiHelper.midiCode(this))
		} catch(Exception ex)
		{
			throw new Exception("cannot convert string " + this + " into midicode")
		} 
	}
    
}

