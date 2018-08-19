package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

public class SnapSelectBehaviour {

	private JShapeLayer<Rectangle2D.Double>[] jShapeLayer;

	public SnapSelectBehaviour(JShapeLayer<Rectangle2D.Double>[] shapeLayer) {
		assert shapeLayer != null;
		jShapeLayer = shapeLayer;
	}

	private Collection<Rectangle2D.Double> col = new ArrayList<Rectangle2D.Double>();

	/**
	 * inform mouse position, if snapped, return true, false otherwise
	 * @param x
	 * @param y
	 * @param tolerance
	 * @return
	 */
	public boolean informMousePosition(double x, double y, double tolerance) {
		
		col.clear();

		for (int i = 0; i < jShapeLayer.length; i++) {

			jShapeLayer[i].find(x, y, tolerance, col);

			if (col.size() > 0) {
				jShapeLayer[i].setSelected(col);
				
				// clear other layer's selection
				for (int k = 0 ; k < jShapeLayer.length ; k ++) {
					if (k != i) {
						jShapeLayer[k].setSelected(null);
					}
				}
				
				return true;
			}
		}
		
		return false;
	}

}
