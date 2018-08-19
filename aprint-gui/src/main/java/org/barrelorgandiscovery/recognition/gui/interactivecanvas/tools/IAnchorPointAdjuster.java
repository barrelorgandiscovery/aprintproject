package org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools;

import java.awt.Shape;
import java.util.List;
import java.util.Set;

import org.barrelorgandiscovery.math.MathVect;

public interface IAnchorPointAdjuster {
	
	public <T extends Shape> void adjust(List<T> shapes, Set<T> selectedShape, MathVect displacement);
	
	
}
