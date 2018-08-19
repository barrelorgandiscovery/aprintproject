
package groovy.aprint.transform

import org.barrelorgandiscovery.scale.*
import org.barrelorgandiscovery.tools.*

/**
 * Note : Object representing a note, from midiCode
 */

class Note implements Comparable<Note> {
	
    int midiCode
    /**
     * get a new note with an upper octave
     */
    def octaveP() { new Note(midiCode : midiCode + 12) }
    
    /**
     * get a new note from the lower octave
     */
    def octaveM() { new Note(midiCode : midiCode - 12) }
    
	/**
	 * add the number of subtones given in parameters
	 * @param tone
	 * @return
	 */
    def plus(int tone) { 
        
        if (midiCode + tone > 127) throw new Exception("cannot add tone, > 127");
        
        new Note(midiCode : midiCode + tone)
        
        
    }
    
	/**
	 * substract the number of subtones given in parameters
	 * @param tone
	 * @return
	 */
    def minus(int tone) { 
        
        if (midiCode - tone < 0) throw new Exception("cannot sub tone, < 0");
        
        new Note(midiCode : midiCode - tone)
        
    }
    /**
     * get a note range from this note to the parametrized note 
     */
    Note[] to(Note n) { (this.midiCode .. n.midiCode).collect { new Note(midiCode:it) } }
    
    /**
     *
     */
    public String toString() { MidiHelper.midiLibelle(midiCode) }
    
    
    boolean equals(o)
    {
    	if (this.is(o)) return true;

   		if (!o || getClass() != o.class) return false;

    	Note that = (Note) o;
    	
    	if (that.midiCode == midiCode) return true;
    	
    	return false;
    }
    
    int hashCode()
    {
    	return midiCode;
    }

	public int compareTo(Note o) {
		return midiCode <=> o.midiCode;
	}
}
