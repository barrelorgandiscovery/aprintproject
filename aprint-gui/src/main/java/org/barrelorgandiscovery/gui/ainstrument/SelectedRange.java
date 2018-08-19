package org.barrelorgandiscovery.gui.ainstrument;


/**
 * Gui class for rendering a key range selection
 * 
 * @author Freydiere Patrice
 */
public class SelectedRange {

	public SelectedRange() {

	}

	public SelectedRange(int start, int end) {
		this.start = start;
		this.end = end;
		swapIfNecessary();
	}

	public int start;
	public int end;

	public void setStart(int start) {
		this.start = start;
		swapIfNecessary();
	}

	public void setEnd(int end) {
		this.end = end;
		swapIfNecessary();
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	private void swapIfNecessary() {
		if (start > end) {
			int tmp = start;
			start = end;
			end = tmp;
		}
	}

	@Override
	public String toString() {
		return "Selected Range :" + start + " -> " + end; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
