package org.barrelorgandiscovery.issues;

import org.barrelorgandiscovery.tools.TimeUtils;
import org.barrelorgandiscovery.virtualbook.Region;

public class IssueRegion extends AbstractSpatialIssue {

	private long start;
	private long length;
	private int begintrack;
	private int endtrack;

	private Region extent;

	public IssueRegion(int type, long start, long length, int begintrack,
			int endtrack) {
		super(type);
		this.start = start;
		this.length = length;
		this.begintrack = begintrack;
		this.endtrack = endtrack;

		Region r = new Region();
		r.start = this.start;
		r.end = this.start + this.length;
		r.beginningtrack = this.begintrack;
		r.endtrack = this.endtrack;
		extent = r;
	}

	public long getStart() {
		return start;
	}

	public long getLength() {
		return length;
	}

	public int getBegintrack() {
		return begintrack;
	}

	public int getEndtrack() {
		return endtrack;
	}

	@Override
	public Region getExtent() {
		return extent;
	}

	@Override
	public String toLabel() {
		return TimeUtils.toMinSecs(getExtent().start);
	}

}
