package org.barrelorgandiscovery.virtualbook.transformation.importer.processor;

import org.barrelorgandiscovery.model.annotations.ParameterOut;
import org.barrelorgandiscovery.model.annotations.ProcessorMethod;
import org.barrelorgandiscovery.model.annotations.ProcessorObject;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiAdvancedEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiEventGroup;

@ProcessorObject(labelName = "MidiNoteGroupIsolator")
public class MidiNoteGroupIsolator {

	/**
	 * Cette fonction isole les évènements
	 * 
	 * @param noteisolator
	 *            le discriminant pour l'isolation
	 * @param groupbyref
	 *            le groupe dans lequel retirer les notes discriminées
	 * @return les notes discriminées
	 */
	public @ProcessorMethod(labelName = "isole")
	MidiEventGroup isole(MidiEventIsolator noteisolator,
			MidiEventGroup groupbyref) {

		MidiEventGroup old = new MidiEventGroup();
		old.addAll(groupbyref);

		MidiEventGroup g = new MidiEventGroup();

		for (MidiAdvancedEvent note : old) {
			if (noteisolator.shouldIsole(note)) {
				groupbyref.remove(note);
				g.add(note);
			}
		}

		return g;
	}

}
