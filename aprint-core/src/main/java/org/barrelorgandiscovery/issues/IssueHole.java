package org.barrelorgandiscovery.issues;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.TimeUtils;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.Region;

/**
 * Problème associé à des notes
 * 
 * @author Freydiere Patrice
 */
public class IssueHole extends AbstractSpatialIssue {

	private Hole[] note = null;

	private Region extent;

	public IssueHole(int type, Hole[] note) {
		super(type);
		this.note = note;

		assert note != null;
		assert note.length > 0;
		
		Region r = new Region();
		r.start = Long.MAX_VALUE;
		r.end = -Long.MAX_VALUE;
		
		r.beginningtrack = 0;
		r.endtrack = 0;
		
		for (int i = 0; i < note.length; i++) {
			r.start = Math.min(r.start, note[i].getTimestamp());
			r.end = Math.max(r.end, note[i].getTimestamp() + note[i].getTimeLength());
		}
		
		extent = r;

	}

	/**
	 * Récupère les trous associé à cette erreur
	 * 
	 * @return
	 */
	public Hole[] getNotes() {
		return note;
	}

	@Override
	public Region getExtent() {
		return extent;
	}
	
	@Override
	public String toLabel() {
		
		String s = TimeUtils.toMinSecs(getExtent().start);
		switch (getType()) {
		case IssuesConstants.HOLE_TOO_SMALL:
			s += Messages.getString("JIssuePresenter.2"); //$NON-NLS-1$
			break;
		case IssuesConstants.INTERLEAVE_TOO_SHORT:
			s += Messages.getString("JIssuePresenter.3"); //$NON-NLS-1$
			break;
		case IssuesConstants.OVERLAPPING_HOLE:
			s += Messages.getString("JIssuePresenter.4"); //$NON-NLS-1$
		}
		
		return s;
	}

}
