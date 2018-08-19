package org.barrelorgandiscovery.gui.aedit.markers;

import java.awt.Cursor;
import java.awt.Image;
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

public class MarkerDeleteTool extends BaseMarkerTool {
	
	private static Logger logger = Logger.getLogger(MarkerDeleteTool.class);
	
	//private JEditableVirtualBookComponent editableComponent;
	
	private Cursor customCursor;
	
	private ISnappingEnvironment snap;
	
	public MarkerDeleteTool(JEditableVirtualBookComponent editableComponent) throws Exception
	{
		super(editableComponent);
		
		Image smallimage = ImageTools.loadImage(getClass(), "applixdelete.png");
		BufferedImage readSmallImage = ImageTools.loadImage(smallimage);
		customCursor = CursorTools.createCursorWithImage(readSmallImage);

		snap = SnappingEnvironmentHelper.createMarkerSnappingEnv(editableComponent);
	}
	
	private boolean isSnapped;
	
	private void toggleSnap(){
		editableComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
	
	private void toggleUnSnap(){
		editableComponent.setCursor(customCursor);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.Tool#unactivated()
	 */
	@Override
	public void unactivated() {
		super.unactivated();
		editableComponent.setCursor(Cursor
				.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	
	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		double x = editableComponent.convertScreenXToCarton(e.getX());

			double y = editableComponent.convertScreenYToCarton(e.getY());
			Point2D.Double pt = new Point2D.Double(x, y);
			if (snap.snapPosition(pt)) {
				if (!isSnapped)
				{
					toggleSnap();
					isSnapped = true;
				} 
				
			} else {
				if (isSnapped)
				{
					toggleUnSnap();
					isSnapped = false;
				}
			}

	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		
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
				MarkerEvent snappedEvent = (MarkerEvent) events.get(0);
				editableComponent.startEventTransaction();
				editableComponent.getUndoStack().push( new GlobalVirtualBookUndoOperation(editableComponent.getVirtualBook(), "Undo Delete Marker", editableComponent));
				editableComponent.getVirtualBook().removeEvent(snappedEvent);
				editableComponent.endEventTransaction();
				toggleUnSnap();
				editableComponent.repaint();
			}
		}

	}
	
}
