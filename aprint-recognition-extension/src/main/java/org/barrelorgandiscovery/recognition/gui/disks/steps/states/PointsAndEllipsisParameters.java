package org.barrelorgandiscovery.recognition.gui.disks.steps.states;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;

import org.barrelorgandiscovery.recognition.math.EllipseParameters;

public class PointsAndEllipsisParameters implements Serializable {

	public EllipseParameters outerEllipseParameters = null;
	
	public EllipseParameters innerEllipseParameters = null;

	public List<Rectangle2D.Double> points = null;

}
