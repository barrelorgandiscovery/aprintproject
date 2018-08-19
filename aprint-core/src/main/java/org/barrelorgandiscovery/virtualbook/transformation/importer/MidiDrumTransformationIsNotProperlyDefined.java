package org.barrelorgandiscovery.virtualbook.transformation.importer;

import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.issues.IssueTrack;
import org.barrelorgandiscovery.issues.IssueTrack.TrackIssue;
import org.barrelorgandiscovery.issues.IssuesConstants;

public class MidiDrumTransformationIsNotProperlyDefined extends
		MidiConversionProblem {

	private int track;

	public MidiDrumTransformationIsNotProperlyDefined(int track) {
		this.track = track;
	}

	@Override
	public AbstractIssue toIssue() {
		IssueTrack issue = new IssueTrack(IssuesConstants.TRACK_ISSUE, track,
				TrackIssue.DRUM_DELAY_COULNT_BE_APPLIED);
		return issue;
	}

	@Override
	public String toString() {
		return "Track " + track
				+ " is not a drum track, delays could not be applied";
	}

}
