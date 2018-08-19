package org.barrelorgandiscovery.virtualbook.transformation.importer;

/**
 * interface defining a midi importer
 * 
 * @author Freydiere Patrice
 * 
 */
public interface MidiImporter {

	MidiConversionResult convert(MidiEventGroup midifile);

}
