package org.barrelorgandiscovery.gui.aedit;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.snapping.HolesSnappingEnvironnement;
import org.barrelorgandiscovery.gui.aedit.snapping.ISnappingEnvironment;
import org.barrelorgandiscovery.tools.SerializeTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * An editable virtual book component, taking a current tool for modifying the
 * virtualbook and having a snapping environment and an undo stack ..
 * 
 * @author Freydiere Patrice
 * 
 */
public class JEditableVirtualBookComponent extends
		JVirtualBookScrollableComponent implements ITransaction {

	private static Logger logger = Logger
			.getLogger(JEditableVirtualBookComponent.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -3489916861928503581L;

	/**
	 * Current activated tool
	 */
	private Tool currentTool = null;

	/**
	 * undo stack
	 */
	private UndoStack undoStack;

	private class HandleMouseEventsForTools implements MouseListener,
			MouseMotionListener, MouseWheelListener, KeyListener {
		// ///////////////////////////////////////////////////////////////////////////
		// Gestion des évènements souris

		public void mouseDragged(MouseEvent e) {
			if (currentTool != null)
				currentTool.mouseDragged(e);
		}

		public void mouseMoved(MouseEvent e) {
			if (currentTool != null)
				currentTool.mouseMoved(e);
		}

		public void mouseClicked(MouseEvent e) {
			if (currentTool != null)
				currentTool.mouseClicked(e);
		}

		public void mouseEntered(MouseEvent e) {
			if (currentTool != null)
				currentTool.mouseEnter(e);
			requestFocus();
		}

		public void mouseExited(MouseEvent e) {
			if (currentTool != null)
				currentTool.mouseExited(e);
		}

		public void mousePressed(MouseEvent e) {
			if (currentTool != null)
				currentTool.mousePressed(e);

		}

		public void mouseReleased(MouseEvent e) {
			if (currentTool != null)
				currentTool.mouseReleased(e);

		}

		/**
		 * Not used !!
		 */
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (currentTool != null)
				currentTool.mouseWheel(e);
		}

		public void keyPressed(KeyEvent e) {
			if (currentTool != null)
				currentTool.keyPressed(e);
		}
		
		public void keyReleased(KeyEvent e) {
			if (currentTool != null)
				currentTool.keyReleased(e);
		}
		
		public void keyTyped(KeyEvent e) {
			if (currentTool != null)
				currentTool.keyTyped(e);
		}
		
	}

	private HandleMouseEventsForTools mouseAndKeyEventListenerForTools = new HandleMouseEventsForTools();

	public JEditableVirtualBookComponent() {
		super();

		this.undoStack = new UndoStack();
		addMouseListener(mouseAndKeyEventListenerForTools);
		addMouseMotionListener(mouseAndKeyEventListenerForTools);
		addKeyListener(mouseAndKeyEventListenerForTools);

	}

	/**
	 * getting a reference of the current undostack ..
	 * 
	 * @return
	 */
	public UndoStack getUndoStack() {
		return undoStack;
	}

	/**
	 * current snapping environnement
	 */
	private ISnappingEnvironment currentSnappingEnvironnement = new HolesSnappingEnvironnement(
			this);

	/**
	 * return the snapping environnement ...
	 * 
	 * @return
	 */
	public ISnappingEnvironment getSnappingEnvironment() {
		return currentSnappingEnvironnement;
	}

	/**
	 * Define the current active tool
	 * 
	 * @param t
	 */
	public void setCurrentTool(Tool t) {

		logger.debug("setting tool :" + t);

		Tool oldTool = currentTool;

		if (currentTool != null) {
			try {
				currentTool.unactivated();
			} catch (Exception ex) {
				logger.error("error unactivating tool :" + ex.getMessage(), ex);
			}
		}

		setCursor(Cursor.getDefaultCursor());

		if (t != null) {
			t.activated();
		}

		currentTool = t;
		if (oldTool != currentTool)
			fireCurrentToolChangedListener(currentTool, oldTool);

	}

	public Tool getCurrentTool() {
		return currentTool;
	}

	private Vector<CurrentToolChanged> toolsChangedListeners = new Vector<CurrentToolChanged>();

	public void addCurrentToolChangedListener(CurrentToolChanged ctc) {
		if (ctc != null)
			toolsChangedListeners.add(ctc);
	}

	public void removeCurrentToolChangedListener(CurrentToolChanged ctc) {
		toolsChangedListeners.remove(ctc);
	}

	protected void fireCurrentToolChangedListener(Tool newTool, Tool oldTool) {
		for (Iterator itCurrentToolChangedListener = toolsChangedListeners
				.iterator(); itCurrentToolChangedListener.hasNext();) {
			CurrentToolChanged ctc = (CurrentToolChanged) itCurrentToolChangedListener
					.next();
			try {
				ctc.currentToolChanged(oldTool, newTool);

			} catch (Throwable t) {
				logger.error(
						"error in current tool change listener :"
								+ t.getMessage(), t);
				BugReporter.sendBugReport();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent#
	 * setVirtualBook(org.barrelorgandiscovery.virtualbook.VirtualBook)
	 */
	@Override
	public void setVirtualBook(VirtualBook carton) {
		super.setVirtualBook(carton);

		logger.debug("set Virtual Book ,  and clear the undostack ...");
		undoStack.clearUndoOperations();
		
		fireVirtualBookChanged(carton);

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Tool c = currentTool;

		if (c != null) {
			// logger.debug("drawing tool graphic");
			c.paintElements(g);
		}

	}

	@Override
	public VirtualBook getVirtualBook() {

		VirtualBook vb = super.getVirtualBook();

		return vb;

	}

	// //////////////////////////////////////////////////////////////////////////
	// managing transaction on the edit

	private Queue<ByteArrayOutputStream> transactionalVirtualBooks = new LinkedList<ByteArrayOutputStream>();

	/* (non-Javadoc)
   * @see org.barrelorgandiscovery.gui.aedit.ITransaction#startEventTransaction()
   */
	@Override
  public void startEventTransaction() {
		logger.debug("startEventTransaction");
		VirtualBook vb = super.getVirtualBook();
		if (vb == null) {
			transactionalVirtualBooks.offer(null);
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			SerializeTools.writeObject(vb, baos);
			transactionalVirtualBooks.offer(baos);
		}
	}

	/* (non-Javadoc)
   * @see org.barrelorgandiscovery.gui.aedit.ITransaction#endEventTransaction()
   */
	@Override
  public void endEventTransaction() {
		logger.debug("endEventTransaction");
		if (transactionalVirtualBooks.size() <= 0)
			logger.error("ERROR IN THE TRANSACTION SYMETRY, check the code");
		
		ByteArrayOutputStream baos = transactionalVirtualBooks.poll();
		VirtualBook vb = super.getVirtualBook();
		if (vb != null && baos != null) {
			
			ByteArrayOutputStream baoscurrentvb = new ByteArrayOutputStream();
			SerializeTools.writeObject(vb, baoscurrentvb);

			if (baoscurrentvb.size() != baos.size()) {
				fireVirtualBookChanged(vb);
				return;
			}

			if (!Arrays.equals(baoscurrentvb.toByteArray(), baos.toByteArray())) {
				fireVirtualBookChanged(vb);
				return;
			}

		} else if (vb == null && baos == null) {
			// nothing to do
			return;
		} else {
			fireVirtualBookChanged(vb);
			return;
		}

	}

	private Vector<IVirtualBookChangedListener> listeners = new Vector<IVirtualBookChangedListener>();

	public void addVirtualBookChangedListener(
			IVirtualBookChangedListener listener) {
		if (listener == null)
			return;
		listeners.add(listener);
	}

	public void removeVirtualBookChangedListener(
			IVirtualBookChangedListener listener) {
		if (listener == null)
			return;
		listeners.remove(listener);
	}

	protected void fireVirtualBookChanged(VirtualBook newvb) {
		logger.debug("Fire VirtualBook Changed");
		
		// selection might have changed, 
		checkSelectionHoleAreStillInBook();
		
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
			IVirtualBookChangedListener l = (IVirtualBookChangedListener) iterator
					.next();
			try {
				l.virtualBookChanged(newvb);
			} catch (Exception ex) {
				logger.error(
						"error when fireing virtualBook change :"
								+ ex.getMessage(), ex);
				BugReporter.sendBugReport();
			}
		}
		// redraw the component
		repaint();
	}

}
