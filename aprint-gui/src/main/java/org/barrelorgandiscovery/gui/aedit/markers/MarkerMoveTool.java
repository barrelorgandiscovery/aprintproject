package org.barrelorgandiscovery.gui.aedit.markers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.GlobalVirtualBookUndoOperation;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.aedit.snapping.ISnappingEnvironment;
import org.barrelorgandiscovery.gui.aedit.snapping.SnappingEnvironmentHelper;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.virtualbook.AbstractEvent;
import org.barrelorgandiscovery.virtualbook.MarkerEvent;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Tool for moving a marker
 * 
 * @author pfreydiere
 *
 */
public class MarkerMoveTool extends BaseMarkerTool {

	private static Logger logger = Logger.getLogger(MarkerMoveTool.class);

	private ISnappingEnvironment snap;

	private Cursor customCursor;

	public MarkerMoveTool(JEditableVirtualBookComponent c) throws Exception {
		super(c);

		Image smallimage = ImageTools.loadImage(getClass(), "applixmove.png");
		BufferedImage readSmallImage = ImageTools.loadImage(smallimage);
		customCursor = CursorTools.createCursorWithImage(readSmallImage);

		snap = SnappingEnvironmentHelper.createMarkerSnappingEnv(c);
	}

	

	/**
	 * Snapped marker
	 */
	private MarkerEvent snappedEvent = null;

	private double feedbackPosition = Double.NaN;

	private boolean isPressed = false;

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);

		isPressed = true;

		double x = editableComponent.convertScreenXToCarton(e.getX());
		double y = editableComponent.convertScreenYToCarton(e.getY());
		Point2D.Double pt = new Point2D.Double(x, y);
		if (snap.snapPosition(pt)) {
			Scale scale = editableComponent.getVirtualBook().getScale();
			int tolerance = 3;
			double pixelToMM = editableComponent.pixelToMM(tolerance);
			long ts = scale.mmToTime(x - pixelToMM);
			
			ArrayList<AbstractEvent> events = editableComponent.getVirtualBook().findEvents(ts,
					ts + scale.mmToTime(tolerance), MarkerEvent.class);
			if (!events.isEmpty()) {
				snappedEvent = (MarkerEvent) events.get(0);
				editableComponent.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
				if (logger.isDebugEnabled())
					logger.debug("marker snapped :" + snappedEvent);
				editableComponent.setHightlight(pt.getX());
			}
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);

		if (snappedEvent != null) {
			// move the current marker
			
			logger.debug("move the marker");
			double x = editableComponent.convertScreenXToCarton(e.getX());
			VirtualBook vb = editableComponent.getVirtualBook();
			Scale scale = vb.getScale();
			long ts = scale.mmToTime(x);

			editableComponent.startEventTransaction();
			editableComponent.getUndoStack().push( new GlobalVirtualBookUndoOperation(editableComponent.getVirtualBook(), "Undo move Marker", editableComponent));
			vb.removeEvent(snappedEvent);
			MarkerEvent newm = new MarkerEvent(ts, snappedEvent.getMarkerName());
			vb.addEvent(newm);
			editableComponent.endEventTransaction();
			
			snappedEvent = null;
			editableComponent.setCursor(customCursor);
			editableComponent.clearHightlight();
			editableComponent.repaint();
		}

		feedbackPosition = Double.NaN;
		isPressed = false;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		double x = editableComponent.convertScreenXToCarton(e.getX());
		if (!isPressed) {

			double y = editableComponent.convertScreenYToCarton(e.getY());
			Point2D.Double pt = new Point2D.Double(x, y);
			if (snap.snapPosition(pt)) {
				
				if (Double.isNaN(feedbackPosition))
				{
					editableComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				}
				
				feedbackPosition = pt.getX();
				logger.debug("snapped");
				// c.repaint();
				
			} else {
				
				if (!Double.isNaN(feedbackPosition))
				{
					editableComponent.setCursor(customCursor);
				}
				
				feedbackPosition = Double.NaN;
			}

		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);
		double x = editableComponent.convertScreenXToCarton(e.getX());
		// pressed
		if (snappedEvent != null) {
			editableComponent.setHightlight(x);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.Tool#activated()
	 */
	@Override
	public void activated() {
		super.activated();
		editableComponent.setCursor(customCursor);
		editableComponent.clearHightlight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.Tool#unactivated()
	 */
	@Override
	public void unactivated() {
		super.unactivated();
		editableComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}
