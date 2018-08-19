package org.barrelorgandiscovery.gui.aedit;

import java.util.Stack;

public class UndoStack {

	private Stack<IUndoableOperation> undoablesOperations;
	
	private Stack<IUndoableOperation> associatedRedoableOperations;

	public UndoStack() {
		this.undoablesOperations = new Stack<>();
		this.associatedRedoableOperations = new Stack<>();
	}

	/**
	 * Push the undoable operation for last action
	 * @param undoOperation
	 */
	public void push(IUndoableOperation undoOperation) {
		undoablesOperations.push(undoOperation);
		// no more "redo" the following state has changed
		associatedRedoableOperations.clear();
	}

	/**
	 * undo last operation if exist
	 */
	public void undoLastOperation() {
		if (!undoablesOperations.isEmpty()) {
			IUndoableOperation op = undoablesOperations.pop();
			if (op instanceof IRedoableOperation) {
				// take a snapshot
				IRedoableOperation ro =(IRedoableOperation)op;
				IUndoableOperation redoUndoOperation = ro.createRedoableOperation();
				assert redoUndoOperation != null;
				associatedRedoableOperations.push(redoUndoOperation);
			}
			op.undo();
		}
	}
	
	/**
	 * redo operation if exists
	 */
	public void redoLastOperation() {
		if (!associatedRedoableOperations.isEmpty()) {
			IUndoableOperation op = associatedRedoableOperations.pop();
			if (op instanceof IRedoableOperation) {
				// take a snapshot
				IRedoableOperation ro =(IRedoableOperation)op;
				IUndoableOperation redoUndoOperation = ro.createRedoableOperation();
				assert redoUndoOperation != null;
				undoablesOperations.push(redoUndoOperation);
			}
			op.undo();
		}
	}
	
	/**
	 * Remove all undo operations
	 */
	public void clearUndoOperations()
	{
		undoablesOperations.clear();
		associatedRedoableOperations.clear();
	}

}
