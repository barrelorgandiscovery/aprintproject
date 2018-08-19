package org.barrelorgandiscovery.gui.atrace;

import java.util.Map;

import javax.swing.ImageIcon;

import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Generic interface for punch optimization
 * 
 * @author Freydiere Patrice
 */
public interface Optimizer {

	
	/**
	 * optimizer title
	 * @return
	 */
	public String getTitle();
	
	
	/**
	 * optimizer icon
	 * @return
	 */
	public ImageIcon getIcon();
	
	/**
	 * Get Parameters associated to optimization
	 * 
	 * @return
	 */
	public Object getDefaultParameters();

	/**
	 * Run optimization on the virtual book
	 * 
	 * @param carton
	 *            le carton à optimiser ..
	 * @return
	 */
	public OptimizerResult optimize(VirtualBook carton) throws Exception;

	/**
	 * run optimization on the virtual book and get feedbacks
	 * 
	 * @param carton the virtualbook
	 * @param progress progress interface to report progress
	 * @param ct cancel tracker to abord operation
	 * @return the result
	 * @throws Exception
	 */
	public OptimizerResult optimize(VirtualBook carton,
			OptimizerProgress progress, ICancelTracker ct) throws Exception;

}
