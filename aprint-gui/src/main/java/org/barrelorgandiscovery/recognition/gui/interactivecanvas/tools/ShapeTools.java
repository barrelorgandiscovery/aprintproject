package org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools;

import java.awt.geom.Rectangle2D;

public class ShapeTools {

	public static Rectangle2D.Double scale2(Rectangle2D.Double rect) {

		double w2 = rect.getCenterX() - rect.x;
		double h2 = rect.getCenterX() - rect.y;

		return new Rectangle2D.Double(rect.x - w2, rect.y - h2, 4 * w2, 4 * h2);

	}

}
