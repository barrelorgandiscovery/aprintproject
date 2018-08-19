package org.barrelorgandiscovery.issues;

import org.barrelorgandiscovery.tools.TimeUtils;

/**
 * An Error at a typical time
 * 
 * @author use
 * 
 */
public class TimedIssue extends AbstractIssue {

	public TimedIssue(int issuetype) {
		super(issuetype);
	}

	private long timeStamp;
	private String description;

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	@Override
	public String toLabel() {
		return TimeUtils.toMinSecs(timeStamp) + " " + getDescription() ;
	}

}
