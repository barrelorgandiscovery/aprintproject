package org.barrelorgandiscovery.repository;

import java.util.Properties;

public class RepositoryFactory {

	/**
	 * Création d'une nouvelle instance de repository
	 * 
	 * @param props
	 *            propriétés de création du Repository
	 * @return
	 */
	public static Repository create(Properties props)
			throws RepositoryException {

		return new RepositoryImpl(props);
	}

}
