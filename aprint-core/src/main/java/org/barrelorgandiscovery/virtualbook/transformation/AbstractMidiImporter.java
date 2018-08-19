package org.barrelorgandiscovery.virtualbook.transformation;

import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiConversionResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;

/**
 * Transposition directe à partir d'un fichier midi ...
 * 
 * @author Freydiere Patrice
 */
public abstract class AbstractMidiImporter extends AbstractTransformation {

	public abstract String getDescription();

	public abstract MidiConversionResult convert(MidiFile midifile);
	

}
