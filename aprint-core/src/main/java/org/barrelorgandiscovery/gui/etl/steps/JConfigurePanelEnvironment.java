package org.barrelorgandiscovery.gui.etl.steps;

import org.barrelorgandiscovery.repository.Repository2;

/**
 * UI definition for a step class to create associated configuration panel.
 * 
 * @author pfreydiere
 */
public class JConfigurePanelEnvironment {

	private Repository2 repository;

	public JConfigurePanelEnvironment(Repository2 repository) {
		this.repository = repository;
	}

	public Repository2 getRepository() {
		return repository;
	}

}
