package org.barrelorgandiscovery.gui.aedit;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;

import org.barrelorgandiscovery.gui.aedit.HolePainterHelper;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class ScaleHolesTool extends Tool {

	private JEditableVirtualBookComponent component;

	private double xOrigin = -1;
	private double currentFactor = 1.0;

	public ScaleHolesTool(JEditableVirtualBookComponent comp) {
		this.component = comp;
	}

	@Override
	public void activated() {
		super.activated();
		this.xOrigin = -1;
		currentFactor = 1.0;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);

		int x = e.getX();
		xOrigin = component.convertScreenXToCarton(x);
		computeFactor(x);

		component.repaint();
	}

	private void computeFactor(int xscreen) {

		if (xOrigin == -1) {
			currentFactor = 1.0;
			return;
		}

		double newx = component.convertScreenXToCarton(xscreen);

		currentFactor = newx / xOrigin;

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);

		double factor = currentFactor;
		xOrigin = -1;// deactivate scale

		// compute factor

		VirtualBook vb = component.getVirtualBook();
		if (vb == null)
			return;

		
		component.startEventTransaction();
		try {
			
			GlobalVirtualBookUndoOperation globalVirtualBookUndoOperation = new GlobalVirtualBookUndoOperation(
					vb, "Scale holes", component);
			
			ArrayList<Hole> newList = new ArrayList<>();
			for (Iterator iterator = vb.getHolesCopy().iterator(); iterator.hasNext();) {
				Hole h = (Hole) iterator.next();
				Hole h2 = new Hole(h.getTrack(), (long) (h.getTimestamp() * factor), (long) (h.getTimeLength() * factor));
				newList.add(h2);
			}

			// todo , move events also ...
			vb.clear();
			vb.addHole(newList);

			component.getUndoStack().push(globalVirtualBookUndoOperation);

		} finally {
			component.endEventTransaction();
		}
		
	
		component.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);

		computeFactor(e.getX());

		component.repaint();

	}

	@Override
	public void paintElements(Graphics graph) {
		if (xOrigin != -1) {

			Graphics2D graphics = (Graphics2D) graph;
			AffineTransform old = component.addCartonTransformAndReturnOldOne(graphics);
			try {

				// compute factor

				VirtualBook vb = component.getVirtualBook();
				if (vb == null)
					return;

				HolePainterHelper helper = new HolePainterHelper(vb);

				for (Iterator iterator = vb.getHolesCopy().iterator(); iterator.hasNext();) {
					Hole h = (Hole) iterator.next();
					Hole h2 = new Hole(h.getTrack(), (long) (h.getTimestamp() * currentFactor),
							(long) (h.getTimeLength() * currentFactor));

					helper.paintNote(graphics, h2);
				}
			} finally {
				graphics.setTransform(old);
			}
		}
	}

}
