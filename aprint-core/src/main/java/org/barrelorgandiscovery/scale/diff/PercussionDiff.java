package org.barrelorgandiscovery.scale.diff;

public class PercussionDiff extends AbstractDiffElement implements ITrackSource {

	private int track;
	
	public PercussionDiff(int track)
	{
		this.track = track;
	}
	
	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSourceTrack() {
		return track;
	}
	
}
