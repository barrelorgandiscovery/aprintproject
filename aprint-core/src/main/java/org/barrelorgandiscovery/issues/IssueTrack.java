package org.barrelorgandiscovery.issues;

public class IssueTrack extends AbstractIssue {

	private int track;
	
	public enum TrackIssue {
		DRUM_DELAY_COULNT_BE_APPLIED	
	}
	
	private TrackIssue trackIssue;
	
	public IssueTrack(int issuetype, int track, TrackIssue trackissue) {
		super(issuetype);
		this.track = track;
		this.trackIssue = trackissue;
	}
	
	public int getTrack() {
		return track;
	}

	@Override
	public String toLabel() {
		return "Track issue :" + trackIssue;
	}

}
