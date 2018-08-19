package org.barrelorgandiscovery.gui.issues;

import org.barrelorgandiscovery.issues.AbstractIssue;

/**
 * listener for issue selection in GUI ....
 * 
 * @author Freydiere Patrice
 * 
 */
public interface IssueSelectionListener {

	public void issueSelected(AbstractIssue issue);

	public void issueDoubleClick(AbstractIssue issue);

}
