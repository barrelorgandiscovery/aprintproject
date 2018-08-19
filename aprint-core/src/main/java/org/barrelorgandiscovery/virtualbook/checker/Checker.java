package org.barrelorgandiscovery.virtualbook.checker;

import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * This interface define a check for issues in the virtual book
 * 
 * @author Freydiere Patrice
 * 
 */
public interface Checker {

	/**
	 * method that launch the check
	 * 
	 * @param book
	 *            the book to check
	 * @return the issue collection
	 * @throws Exception
	 *             an exception can be raised if the check cannot operate
	 *             properly
	 */
	public IssueCollection check(VirtualBook book) throws Exception;

}
