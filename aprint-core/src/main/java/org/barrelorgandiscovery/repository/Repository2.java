package org.barrelorgandiscovery.repository;

import org.barrelorgandiscovery.instrument.InstrumentManager;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.virtualbook.transformation.TransformationManager;

/**
 * Flat interface to repository, Including some saving options also
 * 
 * @author Freydiere Patrice
 * 
 */
public interface Repository2 extends InstrumentManager, ScaleManager,
		TransformationManager {

	/**
	 * Get the name of the repository
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * get the display name (localized)
	 * 
	 * @return
	 */
	public String getLabel();
	

	/**
	 * Add a change listener to the repository
	 * 
	 * @param listener
	 */
	public void addRepositoryChangedListener(RepositoryChangedListener listener);

	/**
	 * remove a repositoryChangeListener
	 * 
	 * @param listener
	 */
	public void removeRepositoryChangedListener(
			RepositoryChangedListener listener);

	/**
	 * Inform if the repository is readonly (cannot modify the scales /
	 * instruments .. and so on ..)
	 * 
	 * @return
	 */
	public boolean isReadOnly();

}
