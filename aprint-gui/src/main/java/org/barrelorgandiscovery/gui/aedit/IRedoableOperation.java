package org.barrelorgandiscovery.gui.aedit;

/**
 * Operation (IUndoable) that can be redone, can provide a redoableOperation 
 * 
 * @author pfreydiere
 * 
 */
public interface IRedoableOperation {

	
	public IUndoableOperation createRedoableOperation();
	
}
