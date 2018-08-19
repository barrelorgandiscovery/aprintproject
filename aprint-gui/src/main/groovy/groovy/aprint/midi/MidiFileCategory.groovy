package groovy.aprint.midi

import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiEventGroup;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;

/**
 * Helper for loading midi file from a file object
 * 
 * @author pfreydiere
 *
 */
@Category(File)
class MidiFileCategory {

	MidiFile load(){
		MidiFileIO.read(this);
	}
	 
}
