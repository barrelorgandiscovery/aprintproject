package org.barrelorgandiscovery.scale.diff;

/**
 * pas de correspondance de la piste source
 * @author use
 *
 */
public class TrackDiff extends AbstractDiffElement implements ITrackSource {

	private int sourceTrack;
	
	public TrackDiff(int sourceTrack)
	{
		this.sourceTrack = sourceTrack;
	}
	
	
	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getSourceTrack() {
		return sourceTrack;
	}
	
}
