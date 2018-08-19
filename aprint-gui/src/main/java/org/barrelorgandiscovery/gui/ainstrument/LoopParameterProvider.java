package org.barrelorgandiscovery.gui.ainstrument;

public interface LoopParameterProvider {

	/**
	 * Provide the loop start
	 * 
	 * @return
	 */
	long getStartLoop();

	/**
	 * Provide the loop end
	 * 
	 * @return
	 */
	long getEndLoop();

}
