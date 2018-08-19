package org.barrelorgandiscovery.scale.diff;

public class TrackNumberDiff extends AbstractDiffElement {

	private int sourceTrackNb;
	private int destinationTrackNb;

	public TrackNumberDiff(int sourceTrackNb, int destinationTrackNb) {
		this.sourceTrackNb = sourceTrackNb;
		this.destinationTrackNb = destinationTrackNb;
	}

	public int getSourceTrackNb() {
		return sourceTrackNb;
	}
	
	public int getDestinationTrackNb() {
		return destinationTrackNb;
	}
	
	@Override
	public String getLabel() {
		return null;
	}

}
