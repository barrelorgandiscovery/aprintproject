package org.barrelorgandiscovery.scale.diff;

public class FixedLengthDiff extends AbstractDiffElement implements ITrackSource{

	private int track;
	
	public FixedLengthDiff(int track)
	{
		this.track = track;
	}
	
	public int getSourceTrack() {
		return track;
	}
	
	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

}
