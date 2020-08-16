package org.barrelorgandiscovery.issues;

import org.barrelorgandiscovery.virtualbook.Region;

public abstract class AbstractSpatialIssue extends AbstractIssue implements
		Comparable<AbstractSpatialIssue> {

	public AbstractSpatialIssue(int issuetype) {
		super(issuetype);
	}

	/**
	 * Récupère l'étendue du problème
	 * 
	 * @return
	 */
	public abstract Region getExtent();

	public int compareTo(AbstractSpatialIssue o) {
		long s = getExtent().start;
		long s1 = o.getExtent().start;
		if (s < s1)
			return -1;
		if (s == s1)
			return 0;
		return 1;
	}
}
