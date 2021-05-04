package org.barrelorgandiscovery.tools.streamstorage;

import java.io.IOException;
import java.io.InputStream;

public interface IStreamRef {

	/**
	 * Ouverture du stream référencé
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract InputStream open() throws IOException;

}