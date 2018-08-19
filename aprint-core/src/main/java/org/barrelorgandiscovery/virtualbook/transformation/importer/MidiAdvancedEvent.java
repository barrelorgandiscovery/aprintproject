package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.io.Serializable;

import org.barrelorgandiscovery.timed.ITimedStamped;

public abstract class MidiAdvancedEvent implements Serializable, ITimedStamped {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9131423870742374000L;

	protected long timestamp;

	public MidiAdvancedEvent(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Get the event timestamp in microsecond
	 * 
	 * @return
	 */
	public long getTimeStamp() {
		return this.timestamp;
	}
	
	
	/**
	 * Set the event timestamp in microsecond
	 * 
	 * @param t
	 */
	public void setTimeStamp(long t) {
		this.timestamp = t;
	}


	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

	
	
	/**
	 * Visitor pattern implementation
	 * 
	 * @param visitor
	 * @throws Exception
	 */
	public abstract void visit(AbstractMidiEventVisitor visitor)
			throws Exception;

	
	
}
