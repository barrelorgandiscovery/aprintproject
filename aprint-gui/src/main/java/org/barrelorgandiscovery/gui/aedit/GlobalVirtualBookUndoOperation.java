package org.barrelorgandiscovery.gui.aedit;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.barrelorgandiscovery.tools.SerializeTools;
import org.barrelorgandiscovery.virtualbook.AbstractEvent;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Global undo operation on the virtual book, this object remember the book before changing
 * something, to be able to restore it if the user undo the operation. This is a quite heavy
 * operation that could be modularize
 *
 * @author pfreydiere
 */
public class GlobalVirtualBookUndoOperation implements IUndoableOperation, IRedoableOperation {

  /** the previous copy */
  private List<Hole> holescopy;

  private AbstractEvent[] events;

  /** undo Message text */
  private String undoTextOperation;

  /** reference to the virtual book */
  private VirtualBook vb;

  /** Reference to editable component, managing the transactions */
  private ITransaction transaction;


  public GlobalVirtualBookUndoOperation(
      VirtualBook vb, String undoTextOperation, ITransaction component) {
    this.holescopy = vb.getOrderedHolesCopy();
    this.events =
        (AbstractEvent[])
            SerializeTools.deepClone(vb.getOrderedEventsByRef().toArray(new AbstractEvent[0]));
    this.undoTextOperation = undoTextOperation;
    this.vb = vb;
    this.transaction = component;
  }

  public String getMessage() {
    return undoTextOperation;
  }

  public void undo() {

    if (transaction != null) transaction.startEventTransaction();
    try {

      for (Hole h : vb.getHolesCopy()) {
        vb.removeHole(h);
      }

      vb.addHole(holescopy);

      // clear all events
      AbstractEvent[] a = vb.getOrderedEventsByRef().toArray(new AbstractEvent[0]);
      for (int i = 0; i < a.length; i++) {
        AbstractEvent abstractEvent = a[i];
        vb.removeEvent(abstractEvent);
      }

      // add saved events
      for (int i = 0; i < events.length; i++) {
        AbstractEvent ae = events[i];
        vb.addEvent(ae);
      }

    } finally {
      if (transaction != null) transaction.endEventTransaction();
    }
  }

  @Override
  public IUndoableOperation createRedoableOperation() {
    return new GlobalVirtualBookUndoOperation(vb, "Redo " + getMessage(), transaction);
  }
}
