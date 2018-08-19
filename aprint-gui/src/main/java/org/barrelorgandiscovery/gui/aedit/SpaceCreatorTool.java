package org.barrelorgandiscovery.gui.aedit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Move the hole forward or backward tool
 * 
 * @author pfreydiere
 * 
 */
public class SpaceCreatorTool extends Tool {

	private static Logger logger = Logger.getLogger(SpaceCreatorTool.class);

	protected JEditableVirtualBookComponent c;

	private HolePainterHelper helper;

	/**
	 * inner class for managing the tool state
	 * 
	 * @author pfreydiere
	 * 
	 */
	private class ToolState {

		private boolean isStarted = false;

		private long startDragTimeStamp;

		private long currentTimeStamp;

		private ArrayList<Hole> holesToMove = null;

		/**
		 * start the drag for the
		 * 
		 * @param mm
		 */
		public void startDragging(long timeStamp) {
			logger.debug("start Dragging");
			ArrayList<Hole> findHoles = c.getVirtualBook().findHoles(timeStamp);

			this.startDragTimeStamp = timeStamp;
			this.currentTimeStamp = timeStamp;
			this.isStarted = true;
			this.holesToMove = findHoles;
			c.repaint();
		}

		public void moveAt(long timeStamp) {
			if (isStarted) {

				this.currentTimeStamp = timeStamp;

				c.repaint();
			}
		}

		public void stop(long timeStamp) {
			if (isStarted) {
				isStarted = false;

				// make the transaction

				c.startEventTransaction();
				try {
					VirtualBook vb = c.getVirtualBook();
					GlobalVirtualBookUndoOperation globalVirtualBookUndoOperation = new GlobalVirtualBookUndoOperation(
							vb, "Book space", c);
					
					vb.shiftAt(this.startDragTimeStamp, timeStamp
								- this.startDragTimeStamp);
					

					c.getUndoStack().push(globalVirtualBookUndoOperation);

				} finally {
					c.endEventTransaction();
				}

				c.repaint();
			}
		}

		public void display(Graphics2D graphics) {
			if (isStarted) {
				assert holesToMove != null;

				AffineTransform old = c
						.addCartonTransformAndReturnOldOne(graphics);
				try {

					for (Iterator iterator = holesToMove.iterator(); iterator
							.hasNext();) {
						Hole h = (Hole) iterator.next();
						Hole h2 = h.newHoleWithOffset(currentTimeStamp
								- startDragTimeStamp);
						helper.paintNote(graphics, h2);

					}
				} finally {
					graphics.setTransform(old);
				}
			}
		}

	}

	private ToolState currentState = new ToolState();

	private Cursor customCursor;
	
	public SpaceCreatorTool(JEditableVirtualBookComponent c) throws Exception {
		this.c = c;

		Image smallimage = ImageTools.loadImage(getClass(), "space.png");
		BufferedImage readSmallImage = ImageTools.loadImage(smallimage);

		customCursor = CursorTools.createCursorWithImage(readSmallImage);
		
	}
	

	@Override
	public void activated() {
		super.activated();
		helper = new HolePainterHelper(c.getVirtualBook());
		currentState.stop(0);
		c.setCursor(customCursor);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.Tool#unactivated()
	 */
	@Override
	public void unactivated() {
		c.setCursor(Cursor
				.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	private long getTimeStamp(MouseEvent e) {
		double mm = c.convertScreenXToCarton(e.getX());
		return c.MMToTime(mm);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		c.setHightlight(c.timestampToMM(getTimeStamp(e)));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		currentState.startDragging(getTimeStamp(e));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		currentState.stop(getTimeStamp(e));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);
		currentState.moveAt(getTimeStamp(e));
	}

	@Override
	public void paintElements(Graphics g) {
		super.paintElements(g);
		Graphics2D g2d = (Graphics2D) g;
		Color oldColor = g2d.getColor();
		try {
			g2d.setColor(Color.YELLOW);
			currentState.display(g2d);
		} finally {
			g2d.setColor(oldColor);
		}
	}

}
