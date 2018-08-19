package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.repository.Repository;

/**
 * This extension point inform about the repository used ..
 * 
 * @author Freydiere Patrice
 * 
 */
public interface InformRepositoryExtensionPoint  extends IExtensionPoint {

	/**
	 * The repository reference is passed to the extension
	 * 
	 * @param repository
	 */
	public void informRepository(Repository repository);

}
