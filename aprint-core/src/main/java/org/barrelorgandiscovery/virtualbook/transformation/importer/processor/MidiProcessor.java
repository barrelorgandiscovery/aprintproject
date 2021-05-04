package org.barrelorgandiscovery.virtualbook.transformation.importer.processor;

import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiEventGroup;

/**
 * Interface définissant un traitement sur un ensemble de note midi ...
 * 
 * @author Freydiere Patrice
 */
public interface MidiProcessor {

	MidiProcessorResult process(MidiEventGroup midifile);

}
