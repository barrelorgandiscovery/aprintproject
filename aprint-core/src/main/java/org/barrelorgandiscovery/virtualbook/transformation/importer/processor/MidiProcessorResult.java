package org.barrelorgandiscovery.virtualbook.transformation.importer.processor;

import java.util.ArrayList;

import org.barrelorgandiscovery.model.annotations.IntermediateResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiEventGroup;

@IntermediateResult
public class MidiProcessorResult {

	public MidiEventGroup result = null;
	public ArrayList<MidiProcessorError> errors = null;

}
