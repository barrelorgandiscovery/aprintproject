package org.barrelorgandiscovery.virtualbook.transformation.importer.processor;

import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiAdvancedEvent;

public interface MidiEventIsolator {

	/**
	 * Méthode retournant un boolean indiquant si l'évènement doit être séparé
	 * 
	 * @param note
	 * @return
	 */
	boolean shouldIsole(MidiAdvancedEvent note);

}
