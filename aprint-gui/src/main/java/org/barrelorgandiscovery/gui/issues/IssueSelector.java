package org.barrelorgandiscovery.gui.issues;

import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.issues.AbstractSpatialIssue;
import org.barrelorgandiscovery.issues.IssueLayer;
import org.barrelorgandiscovery.issues.TimedIssue;
import org.barrelorgandiscovery.virtualbook.Region;

public class IssueSelector implements IssueSelectionListener {

	private IssueLayer il;
	private JVirtualBookScrollableComponent pianoroll;

	public IssueSelector(IssueLayer il,
			JVirtualBookScrollableComponent pianoroll) {
		this.il = il;
		this.pianoroll = pianoroll;
	}

	public void issueSelected(AbstractIssue issue) {
		if (issue == null) {
			il.resetSelectedIssues();
		} else {
			il.setSelectedIssues(new AbstractIssue[] { issue });
		}

		pianoroll.repaint();
		return;
	}

	public void issueDoubleClick(AbstractIssue issue) {

		if (issue == null)
			return;

		if (issue instanceof AbstractSpatialIssue) {
			AbstractSpatialIssue asi = (AbstractSpatialIssue) issue;

			int width = pianoroll.getWidth();
			double length = pianoroll.pixelToMM(width);

			Region extent = asi.getExtent();

			double center = pianoroll
					.timeToMM((extent.start + extent.end) / 2);

			pianoroll.setXoffset(center - length / 2);

			il.setVisible(true);

			pianoroll.repaint();
		} else if (issue instanceof TimedIssue) {

			TimedIssue ti = (TimedIssue) issue;
			int width = pianoroll.getWidth();
			double length = pianoroll.pixelToMM(width);

			long ts = ti.getTimeStamp();
			double center = pianoroll.timeToMM(ts);

			pianoroll.setXoffset(center - length / 2);

		}
	}
}