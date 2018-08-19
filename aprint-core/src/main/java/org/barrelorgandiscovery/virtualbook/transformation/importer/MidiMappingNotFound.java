package org.barrelorgandiscovery.virtualbook.transformation.importer;

import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.issues.IssuesConstants;
import org.barrelorgandiscovery.issues.TimedIssue;
import org.barrelorgandiscovery.messages.Messages;

public class MidiMappingNotFound extends MidiConversionProblem {

	public MidiNote mn = null;

	public MidiMappingNotFound(MidiNote mn) {
		this.mn = mn;
	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(Messages.getString("MidiMappingNotFound.0")); //$NON-NLS-1$
		if (mn == null) {
			sb.append("null"); //$NON-NLS-1$
		} else {
			sb.append(mn.toString());
		}
		return sb.toString();
	}

	@Override
	public AbstractIssue toIssue() {
		TimedIssue ti = new TimedIssue(IssuesConstants.NOTE_MISSING);
		ti.setTimeStamp(mn.getTimeStamp());
		ti.setDescription(toString());
		return ti;
	}

}
