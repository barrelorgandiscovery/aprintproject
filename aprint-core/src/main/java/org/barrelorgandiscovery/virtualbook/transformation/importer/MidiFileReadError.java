package org.barrelorgandiscovery.virtualbook.transformation.importer;

import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.issues.IssuesConstants;
import org.barrelorgandiscovery.issues.TimedIssue;

public class MidiFileReadError {

	public static enum ErrorCode {
		BAD_NOTE_START, BAD_NOTE_END

	}

	public ErrorCode code;
	public long timeStamp;
	public String description;

	/**
	 * Convert to an issue
	 * 
	 * @return
	 */
	public AbstractIssue toIssue() {
		TimedIssue timedIssue = new TimedIssue(IssuesConstants.MIDI_ISSUE);
		timedIssue.setTimeStamp(timeStamp);
		timedIssue.setDescription(description);

		return timedIssue;
	}

}
