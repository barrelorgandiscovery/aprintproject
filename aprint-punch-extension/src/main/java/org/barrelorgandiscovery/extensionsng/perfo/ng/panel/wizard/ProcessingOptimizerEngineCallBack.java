package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import org.barrelorgandiscovery.gui.atrace.OptimizerResult;

/**
 * call back for optimizer engine
 * 
 * @author use
 * 
 */
public interface ProcessingOptimizerEngineCallBack {

	/**
	 * called when progress change
	 * @param percentage
	 * @param message
	 */
	void progressUpdate(double percentage, String message);

	/**
	 * called when the process ended
	 * @param result
	 */
	void processEnded(OptimizerResult result);

}
