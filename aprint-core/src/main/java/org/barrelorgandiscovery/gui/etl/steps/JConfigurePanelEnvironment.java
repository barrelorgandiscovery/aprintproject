package org.barrelorgandiscovery.gui.etl.steps;

import org.barrelorgandiscovery.repository.Repository2;

/**
 * Environnement UI permettant à une classe Step de créer son composant visuel
 * de configuration, cet objet est passé en paramètre du panel pour la saisie de
 * la configuration d'un step
 * 
 * @author pfreydiere
 * 
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
