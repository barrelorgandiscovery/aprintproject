package org.barrelorgandiscovery.gui.atrace;

/**
 * bounding box for elements, to permit to search using the quad tree
 * 
 * @author pfreydiere
 *
 */
public class Extent {

	public final double xmin;
	public final double ymin;
	public final double xmax;
	public final double ymax;

	public Extent(double xmin, double ymin, double xmax, double ymax) {
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
	}

}
