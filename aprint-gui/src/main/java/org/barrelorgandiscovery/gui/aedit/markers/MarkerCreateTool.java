package org.barrelorgandiscovery.gui.aedit.markers;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import org.barrelorgandiscovery.gui.aedit.GlobalVirtualBookUndoOperation;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.aedit.snapping.ISnappingEnvironment;
import org.barrelorgandiscovery.gui.aedit.snapping.SnappingEnvironmentHelper;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.virtualbook.MarkerEvent;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Tool for creating a marker
 * 
 * @author use
 * 
 */
public class MarkerCreateTool extends BaseMarkerTool {

	
	private Cursor customCursor;

	private ISnappingEnvironment snap;

	public MarkerCreateTool(JEditableVirtualBookComponent editableComponent)
			throws Exception {
		super(editableComponent);
		
		Image smallimage = ImageTools.loadImage(getClass(), "applix.png");
		BufferedImage readSmallImage = ImageTools.loadImage(smallimage);

		customCursor = CursorTools.createCursorWithImage(readSmallImage);

		snap = SnappingEnvironmentHelper
				.createMesureAndHolesSnappingEnv(editableComponent);

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
		editableComponent.setCursor(Cursor
				.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.gui.aedit.Tool#mouseMoved(java.awt.event.MouseEvent
	 * )
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);

		VirtualBook vb = editableComponent.getVirtualBook();
		if (vb == null)
			return;

		double posx = editableComponent.convertScreenXToCarton(e.getX());

		Point2D.Double pt = new Point2D.Double(posx, e.getY());

		snap.snapPosition(pt);

		editableComponent.setHightlight(pt.getX());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.gui.aedit.Tool#mouseReleased(java.awt.event.
	 * MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);

		if ((e.getButton() & MouseEvent.BUTTON1) != 0) {
			VirtualBook vb = editableComponent.getVirtualBook();
			if (vb == null)
				return;

			double posx = editableComponent.convertScreenXToCarton(e.getX());

			Point2D.Double pt = new Point2D.Double(posx, e.getY());
			snap.snapPosition(pt);

			long timeStamp = vb.getScale().mmToTime(posx);

			String newMarkerName = JOptionPane
					.showInputDialog("Nom du marker ?");
			if (newMarkerName != null && !"".equals(newMarkerName)) {
				editableComponent.startEventTransaction();
				editableComponent.getUndoStack().push(
						new GlobalVirtualBookUndoOperation(editableComponent
								.getVirtualBook(), "Undo Create Marker",
								editableComponent));
				try {
					vb.addEvent(new MarkerEvent(timeStamp, newMarkerName));
				} finally {
					editableComponent.endEventTransaction();
				}
				editableComponent.repaint();
			}
		}
	}

}
