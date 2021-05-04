package org.barrelorgandiscovery.extensionsng.perfo.cad.canvas;

/**
 * dissociate the application and power properties
 * 
 * @author pfreydiere
 *
 */
public interface PowerCallBack {

	/**
	 * get the power for a specific layer
	 * 
	 * @param layer
	 * @return
	 */
	double getPowerForLayer(String layer);
	
	double getSpeedForLayer(String layer);

}
