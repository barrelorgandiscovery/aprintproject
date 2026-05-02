package org.barrelorgandiscovery.gui.ainstrument.pianoroll;

import org.barrelorgandiscovery.gui.ainstrument.SelectedRange;

/**
 * Fired when the user finishes dragging or resizing a mapped key range on the
 * piano roll (see {@link JPianoRollComponent}).
 */
public interface PianoRollRangeEditListener {

	/**
	 * @param clientTag optional object passed to
	 *        {@link JPianoRollComponent#addRange(SelectedRange, Object)} (e.g. a
	 *        sound sample).
	 */
	void rangeBoundsChangeCommitted(SelectedRange range, Object clientTag, int start, int end);
}
