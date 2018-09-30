package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

/**
 * displacement command
 * the Y is screen oriented
 * the X is screen oriented also
 * 
 * the coordinates are not ordered by machine AXIS (it can depends on machine construction)
 * 
 * @author pfreydiere
 *
 */
public interface XYCommand {

	/**
	 * get X
	 * @return
	 */
	double getX();
	
	/**
	 * get Y
	 * @return
	 */
	double getY();
	
}
