package org.barrelorgandiscovery.gui.issues;

import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * 
 * Interface for adding additional checks
 * 
 */
public interface IssueRevalidateHook {

	void addAdditionalChecks(VirtualBook vb,
			IssueCollection resultIssueCollection);

}
