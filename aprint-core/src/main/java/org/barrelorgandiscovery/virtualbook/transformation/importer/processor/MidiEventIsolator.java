package org.barrelorgandiscovery.virtualbook.transformation.importer.processor;

import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiAdvancedEvent;

public interface MidiEventIsolator {

	/**
	 * M�thode retournant un boolean indiquant si l'�v�nement doit �tre s�par�
	 * 
	 * @param note
	 * @return
	 */
	boolean shouldIsole(MidiAdvancedEvent note);

}
