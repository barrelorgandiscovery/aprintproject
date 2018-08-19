package org.barrelorgandiscovery.issues;

import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiAdvancedEvent;

public class IssueMidiTranslation extends AbstractIssue {

	private MidiAdvancedEvent adve;

	public IssueMidiTranslation(MidiAdvancedEvent adve) {
		super(IssuesConstants.MIDI_ISSUE);
		this.adve = adve;
	}

	@Override
	public String toLabel() {
		return "Midi Error/Warning :" + adve;
	}

}
