package org.barrelorgandiscovery.gui.aprintng.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * 
 * Interface implemented for getting a new issue validation
 * 
 */
public interface VirtualBookIssueRevalidation  extends IExtensionPoint{

	/**
	 * This method is implemented to add additional checks
	 * 
	 * @param vb
	 * @param ic
	 */
	void fillIssuesForVirtualBook(VirtualBook vb, IssueCollection ic);

}
