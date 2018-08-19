package org.barrelorgandiscovery.gui.aedit;

/**
 * Operation that can be undone
 * 
 * @author pfreydiere
 * 
 */
public interface IUndoableOperation {

	/**
	 * informative message about the undo operation
	 * 
	 * @return
	 */
	public String getMessage();

	/**
	 * undo the operation
	 */
	public void undo();

}
