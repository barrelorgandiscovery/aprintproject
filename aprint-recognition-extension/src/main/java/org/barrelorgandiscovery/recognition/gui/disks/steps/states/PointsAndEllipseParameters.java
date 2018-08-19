package org.barrelorgandiscovery.recognition.gui.disks.steps.states;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;

import org.barrelorgandiscovery.recognition.math.EllipseParameters;

public class PointsAndEllipseParameters implements Serializable {

	public EllipseParameters ellipseParameters = null;

	public List<Rectangle2D.Double> points = null;

}
