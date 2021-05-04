package org.barrelorgandiscovery.gui.aedit;

import java.awt.event.MouseEvent;

import org.barrelorgandiscovery.virtualbook.Position;

/**
 * This tool permit a selection in the book
 * 
 * @author Freydiere Patrice
 */
public class SelectionTool extends Tool {

	public static interface SelectionListener {
		void blockSelectionDone();
	}

	private JVirtualBookScrollableComponent c = null;

	private SelectionListener listener = null;

	private long positionstart = 0;

	/**
	 * Constructor
	 * 
	 * @param c the component
	 */
	public SelectionTool(JVirtualBookScrollableComponent c) {
		this(c, null);
	}

	/**
	 * Constructor with selection listener
	 * 
	 * @param c
	 * @param listener
	 */
	public SelectionTool(JVirtualBookScrollableComponent c, SelectionListener listener) {
		this.c = c;
		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.gui.aedit.Tool#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {

		Position p = c.query(e.getX(), e.getY());

		if (p == null) {
			return;
		}

		positionstart = p.position;

		c.setBlockSelection(p.position, 0);
		c.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.gui.aedit.Tool#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		Position p = c.query(e.getX(), e.getY());
		if (p == null)
			return;

		c.setBlockSelection(positionstart, p.position - positionstart);
		c.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.gui.aedit.Tool#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		Position p = c.query(e.getX(), e.getY());
		if (p == null) {
			return;
		}

		long s = positionstart;
		long length = p.position - s;

		c.setBlockSelection(s, length);

		if (listener != null) {
			listener.blockSelectionDone();
		}
		c.repaint();
	}

}
