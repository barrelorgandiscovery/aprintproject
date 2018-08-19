package org.barrelorgandiscovery.issues;

public interface IssueCollectionListener {

	/**
	 * Signal issues changed
	 * 
	 * @param ic
	 *            the new issue collection
	 */
	void issuesChanged(IssueCollection ic);

}
