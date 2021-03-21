package org.barrelorgandiscovery.model.enhanced;

import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;

/**
 * transport type for hole and associated scale, this is usefull in the model editor
 * 
 * @author pfreydiere
 *
 */
public class HoleWithScale extends Hole {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7995514808448741992L;

	private Scale associatedScale;

	public HoleWithScale(Hole n, Scale associatedScale) {
		super(n);
		assert associatedScale != null;
		this.associatedScale = associatedScale;
	}

	public HoleWithScale(int piste, long timestamp, long length, Scale associatedScale) {
		super(piste, timestamp, length);
		this.associatedScale = associatedScale;
		assert associatedScale != null;
	}

	public AbstractTrackDef getTrackDefinition() {
		return associatedScale.getTracksDefinition()[this.getTrack()];
	}
	
	public Scale getScale() {
		return this.associatedScale;
	}

}
