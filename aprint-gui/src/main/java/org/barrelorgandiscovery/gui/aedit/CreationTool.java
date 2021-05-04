package org.barrelorgandiscovery.gui.aedit;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.barrelorgandiscovery.gui.aedit.snapping.ISnappingEnvironment;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.Position;

/**
 * Tool for cutting the virtual book
 * 
 * @author Freydiere Patrice
 * 
 */
public class CreationTool extends Tool {

	public interface CreationToolAction {
		void handleAction(Hole n, boolean isRemove);
	}

	private JEditableVirtualBookComponent c;

	private boolean hasstarted = false;
	private long positionstart = -1;
	private int pistestart = -1;
	private long feedbackEnd = -1;

	private UndoStack us;

	private ISnappingEnvironment se;

	/**
	 * Cursor for making hole
	 */
	private Cursor customCursor;

	/**
	 * Cursor for remove hole
	 */
	private Cursor customCursorRemove;

	CreationToolAction action = new CreationToolAction() {

		@Override
		public void handleAction(Hole n, boolean isRemove) {

			c.startEventTransaction();
			try {

				if (isRemove) {
					// remove hole part
					us.push(new GlobalVirtualBookUndoOperation(c.getVirtualBook(), "Undo cut holes", c));
					c.getVirtualBook().cutHoles(n);

				} else {
					us.push(new GlobalVirtualBookUndoOperation(c.getVirtualBook(), "Undo create hole", c));
					c.getVirtualBook().addAndMerge(n);
				}

			} finally {
				c.endEventTransaction();
			}
		}

	};

	/**
	 * default creation tool
	 * 
	 * @param c
	 * @param us
	 * @param se
	 * @throws Exception
	 */
	public CreationTool(JEditableVirtualBookComponent c, UndoStack us, ISnappingEnvironment se) throws Exception {
		this(c, us, se, null);
	}

	/**
	 * create a tool with custom creation action, this permit to do several things
	 * with the created or modified hole
	 * 
	 * @param c
	 * @param us
	 * @param se
	 * @param customCreationToolAction
	 * @throws Exception
	 */
	public CreationTool(JEditableVirtualBookComponent c, UndoStack us, ISnappingEnvironment se,
			CreationToolAction customCreationToolAction) throws Exception {
		this.c = c;
		this.us = us;
		this.se = se;

		customCursor = CursorTools.createCursorWithImage(ImageTools.loadImage(CreationTool.class.getResource("tablet.png")));

		customCursorRemove = CursorTools
				.createCursorWithImage(ImageTools.loadImage(CreationTool.class.getResource("tabletremove.png")));

		if (customCreationToolAction != null) {
			this.action = customCreationToolAction;
		}

	}

	@Override
	public void activated() {
		super.activated();
		c.setCursor(customCursor);
	}

	@Override
	public void unactivated() {
		c.setCursor(Cursor.getDefaultCursor());
		super.unactivated();
	}

	/**
	 * reset tool state
	 */
	private void reinitPosition() {
		hasstarted = false;
		positionstart = -1;
		pistestart = -1;
		feedbackEnd = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.gui.aedit.Tool#mousePressed(java.awt.event.MouseEvent )
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		Position p = c.query(e.getX(), e.getY());
		if (p == null)
			return;

		Point2D.Double ps = new Point2D.Double(c.timestampToMM(p.position), c.convertScreenYToCarton(e.getY()));

		snapPosition(e, ps);

		positionstart = c.MMToTime(ps.x);
		pistestart = p.track;
		hasstarted = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.gui.aedit.Tool#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {

		Position p = c.query(e.getX(), e.getY());
		if (p == null)
			return;

		Point2D.Double d = new Point2D.Double(c.convertScreenXToCarton(e.getX()), c.convertScreenYToCarton(e.getY()));

		snapPosition(e, d);

		c.setHightlight(d.x);
		c.repaint();
	}

	private void snapPosition(MouseEvent e, Point2D.Double d) {
		if (se == null)
			return;

		if (((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0)) {
			se.snapPosition(d);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		if (!hasstarted)
			return;

		Position p = c.query(e.getX(), e.getY());
		if (p == null)
			return;
		Point2D.Double d = new Point2D.Double(c.convertScreenXToCarton(e.getX()), c.convertScreenYToCarton(e.getY()));

		snapPosition(e, d);

		feedbackEnd = p.position;

		c.setHightlight(d.x);
		c.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.gui.aedit.Tool#mouseReleased(java.awt.event.MouseEvent )
	 */
	@Override
	public void mouseReleased(MouseEvent e) {

		if (!hasstarted)
			return;
		try {

			Position p = c.query(e.getX(), e.getY());
			if (p == null)
				return;

			feedbackEnd = -1;

			Point2D.Double d = new Point2D.Double(c.convertScreenXToCarton(e.getX()),
					c.convertScreenYToCarton(e.getY()));

			snapPosition(e, d);

			long length = c.MMToTime(d.x) - positionstart;

			if (length < 0) {
				length = -length;
				assert length > 0;
				positionstart -= length;
			}
			// comfort, don't take too small holes into account

			double lengthmm = c.timestampToMM(length);

			int pixelwidth = c.MmToPixel(lengthmm);

			if (pixelwidth < 2) {
				reinitPosition();
				c.repaint();
				return;
			}

			Hole n = new Hole(pistestart, positionstart, length);

			boolean isRemove = e.getButton() == MouseEvent.BUTTON3 || removeKeyModifier;

			assert action != null;
			action.handleAction(n, isRemove);

			reinitPosition();
		} finally {
			hasstarted = false;
		}
	}

	private boolean removeKeyModifier = false;

	@Override
	public void keyPressed(KeyEvent e) {

		if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
			removeKeyModifier = true;
			c.setCursor(customCursorRemove);
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == 0) {
			c.setCursor(customCursor);
			removeKeyModifier = false;
		}
	}

	@Override
	public void paintElements(Graphics g) {

		if (feedbackEnd != -1 && hasstarted) {

			int start = c.convertCartonToScreenX(c.timestampToMM(positionstart));
			int end = c.convertCartonToScreenX(c.timestampToMM(feedbackEnd));

			int y = c.convertCartonToScreenY(c.trackToMM(pistestart));

			g.drawRect(start, y - 5, end - start, 10);

		}

	}
}
