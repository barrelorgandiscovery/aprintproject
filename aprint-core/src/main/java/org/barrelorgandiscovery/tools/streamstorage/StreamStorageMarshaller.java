package org.barrelorgandiscovery.tools.streamstorage;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface defining a stream storage packager ...
 * 
 * @author Freydiere Patrice
 */
public interface StreamStorageMarshaller {

	/**
	 * Pack the stream storage in an outputStream
	 * 
	 * @param inStorage
	 *            the storage to marshall
	 * @param os
	 *            the output stream
	 * @throws Exception
	 */
	public void pack(StreamStorage inStorage, OutputStream os) throws Exception;

	/**
	 * Unpack the stream in an outStorage ...
	 * 
	 * @param is
	 * @param outStorage
	 */
	public void unpack(InputStream is, StreamStorage outStorage) throws Exception;

}
