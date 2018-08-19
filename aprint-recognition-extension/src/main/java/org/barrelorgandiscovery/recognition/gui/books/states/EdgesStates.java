package org.barrelorgandiscovery.recognition.gui.books.states;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;

public class EdgesStates implements Serializable {

	public List<Point2D.Double> top;
	public List<Point2D.Double> bottom;
	
	public boolean viewInverted;
	
}
