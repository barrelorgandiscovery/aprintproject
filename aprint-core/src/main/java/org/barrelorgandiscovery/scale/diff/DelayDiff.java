package org.barrelorgandiscovery.scale.diff;

public class DelayDiff extends AbstractDiffElement implements ITrackSource {

	private int track;

	public DelayDiff(int track) {
		this.track = track;
	}
	
	public int getSourceTrack() {
		return track;
	}
	

	@Override
	public String getLabel() {
		return null;
	}

}
