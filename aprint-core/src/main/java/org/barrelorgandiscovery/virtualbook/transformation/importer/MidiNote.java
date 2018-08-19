package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.io.Serializable;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.timed.ITimedLength;
import org.barrelorgandiscovery.timed.ITimedStamped;
import org.barrelorgandiscovery.tools.HashCodeUtils;

public class MidiNote extends MidiAdvancedEvent implements Serializable, ITimedLength {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8808270725323190517L;

	private long longueur;
	private int midiNote;
	private int track;
	private int channel;

	public int getMidiNote() {
		return midiNote;
	}

	public int getTrack() {
		return track;
	}

	public void setTrack(int t) {
		track = t;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int c) {
		channel = c;
	}

	public MidiNote(long timestamp, long longueur, int midinote, int track, int channel) {
		super(timestamp);
		this.longueur = longueur;

		this.midiNote = midinote;
		this.track = track;
		this.channel = channel;
	}

	public long getLength() {
		return longueur;
	}

	public void setLength(long l) {
		this.longueur = l;
	}

	@Override
	public long getTimeLength() {
		return this.longueur;
	}

	@Override
	public String toString() {
		return Messages.getString("MidiNote.0") + track + "," + " " + "Channel : " + channel + "  " //$NON-NLS-1$ //$NON-NLS-2$
				+ Messages.getString("MidiNote.1") + timestamp
				+ "  " + Messages.getString("MidiNote.2") + midiNote + "  " + Messages.getString("MidiNote.3") //$NON-NLS-1$ //$NON-NLS-2$
				+ longueur;
	}

	@Override
	public int hashCode() {
		int seed = HashCodeUtils.SEED;
		seed = HashCodeUtils.hash(seed, timestamp);
		seed = HashCodeUtils.hash(seed, longueur);
		seed = HashCodeUtils.hash(seed, midiNote);
		seed = HashCodeUtils.hash(seed, track);
		seed = HashCodeUtils.hash(seed, channel);
		return seed;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		MidiNote n = (MidiNote) obj;

		return n.timestamp == timestamp && n.longueur == longueur && n.midiNote == midiNote && n.track == track
				&& n.channel == channel;
	}

	public void visit(AbstractMidiEventVisitor visitor) throws Exception {
		visitor.visit(this);
	}

}
