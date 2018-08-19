package org.barrelorgandiscovery.repository;

import java.util.Properties;

public class RepositoryFactory {

	/**
	 * Cr�ation d'une nouvelle instance de repository
	 * 
	 * @param props
	 *            propri�t�s de cr�ation du Repository
	 * @return
	 */
	public static Repository create(Properties props)
			throws RepositoryException {

		return new RepositoryImpl(props);
	}

}
