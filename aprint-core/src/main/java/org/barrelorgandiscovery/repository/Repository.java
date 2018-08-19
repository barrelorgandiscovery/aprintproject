package org.barrelorgandiscovery.repository;

import org.barrelorgandiscovery.instrument.InstrumentManager;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.virtualbook.transformation.TransformationManager;

public interface Repository {
	
	/**
	 * get the repository name ...
	 */
	String getName(); 

	/**
	 * repository label
	 * @return
	 */
	String getLabel();
	
	/**
	 * Scale manager of the repository
	 * @return
	 */
	ScaleManager getScaleManager();

	/**
	 * Transformation Manager of the repository
	 * @return
	 */
	TransformationManager getTranspositionManager();

	/**
	 * Instrument manager of the repository
	 * @return
	 */
	InstrumentManager getInstrumentManager();

}
