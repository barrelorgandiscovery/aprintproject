package org.barrelorgandiscovery.virtualbook;

import org.barrelorgandiscovery.tools.HashCodeUtils;
import org.barrelorgandiscovery.tools.StringTools;

/**
 * Event that handle a marker name, object handled by value
 * 
 * @author pfreydiere
 * 
 */
public class MarkerEvent extends AbstractEvent {

	private String markerName;

	public MarkerEvent(long timestamp, String name) {
		super(timestamp);
		this.markerName = name;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4237286261634883989L;

	public String getMarkerName() {
		return markerName;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || (!(obj instanceof MarkerEvent)))
			return false;

		MarkerEvent other = (MarkerEvent) obj;

		return getTimestamp() == other.getTimestamp()
				&& StringTools.equals(markerName, other.markerName);

	}

	@Override
	public int hashCode() {
		int h = HashCodeUtils.SEED;
		h = HashCodeUtils.hash(h, getTimestamp());
		h = HashCodeUtils.hash(h, markerName);
		return h;
	}
	
	@Override
	public String toString() {
		return getTimestamp() +  " : MarkerEvent " + markerName ;
	}

}
