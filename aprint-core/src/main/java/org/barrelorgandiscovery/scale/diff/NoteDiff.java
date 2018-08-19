package org.barrelorgandiscovery.scale.diff;

import org.barrelorgandiscovery.scale.eval.Note;

/**
 * indique une différence de note entre les deux gammes
 * 
 * @author pfreydiere
 * 
 */
public class NoteDiff extends AbstractDiffElement implements ITrackSource {

	private int initialSourceTrack;
	private Note sourceNote;
	private Note destinationNote;

	public NoteDiff(int initialSourceTrack, Note sourceNote, Note destinationNote) {
		assert sourceNote != null;
		assert destinationNote != null;
		this.initialSourceTrack = initialSourceTrack;
		this.sourceNote = sourceNote;
		this.destinationNote = destinationNote;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSourceTrack() {
		return initialSourceTrack;
	}

	public Note getSourceNote() {
		return sourceNote;
	}

	public Note getDestinationNote() {
		return destinationNote;
	}

}
