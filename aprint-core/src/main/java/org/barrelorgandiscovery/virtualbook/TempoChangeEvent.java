package org.barrelorgandiscovery.virtualbook;

/**
 * The Class TempoChangeEvent. an event indicating a tempo change in the book
 */
public class TempoChangeEvent extends AbstractEvent {

	/** The noirlength. */
	private long noirlength;

	/**
	 * Instantiates a new tempo change event.
	 * 
	 * @param timestamp
	 *            the timestamp
	 * @param noirlength
	 *            the noirlength
	 */
	public TempoChangeEvent(long timestamp, long noirlength) {
		super(timestamp);
		this.noirlength = noirlength;
	}

	/**
	 * Get the length
	 * 
	 * @return the noir length
	 */
	public long getNoirLength() {
		return this.noirlength;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TempoChangeEvent :").append(this.getTimestamp()).append(" ")
				.append(noirlength);
		return sb.toString();
	}

}
