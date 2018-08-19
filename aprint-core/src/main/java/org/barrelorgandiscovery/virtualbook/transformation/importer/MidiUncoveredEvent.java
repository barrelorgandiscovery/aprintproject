package org.barrelorgandiscovery.virtualbook.transformation.importer;

import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.issues.IssueMidiTranslation;

public class MidiUncoveredEvent extends MidiConversionProblem {

	private MidiAdvancedEvent advEvent;

	public MidiUncoveredEvent(MidiAdvancedEvent advEvent) {
		super();
		this.advEvent = advEvent;
	}

	@Override
	public AbstractIssue toIssue() {
		IssueMidiTranslation issue = new IssueMidiTranslation(advEvent);
		return issue;
	}

}
