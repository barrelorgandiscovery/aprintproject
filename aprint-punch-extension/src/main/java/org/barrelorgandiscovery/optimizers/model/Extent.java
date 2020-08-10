package org.barrelorgandiscovery.optimizers.model;

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

	/**
	 * return a new extend which is the union of this extend and the passed
	 * parameter
	 * 
	 * @param extend
	 * @return
	 */
	public Extent union(Extent extend) {
		return new Extent(Math.min(xmin, extend.xmin), Math.min(ymin, extend.ymin), Math.max(xmax, extend.xmax),
				Math.max(ymax, extend.ymax));
	}

}
