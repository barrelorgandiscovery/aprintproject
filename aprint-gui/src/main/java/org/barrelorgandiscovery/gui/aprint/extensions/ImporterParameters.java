package org.barrelorgandiscovery.gui.aprint.extensions;

/**
 * Interface defining the parameters associated to an import, if defined, aprint
 * ask for the parameters and inform the importer of the parameters
 * 
 * @author Freydiere Patrice
 * 
 */
public interface ImporterParameters {

	/**
	 * Get the bean that define the parameters of the importer
	 * 
	 * @return
	 */
	public Object getParametersInstanceBean();

	/**
	 * Set the parameters to use in the import
	 * 
	 * @param parameters
	 */
	public void setParametersToUse(Object parameters);

}
