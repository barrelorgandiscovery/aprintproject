package org.barrelorgandiscovery.tools.streamstorage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface defining a stream storage management
 * @author use
 *
 */
public interface StreamStorage {

	/**
	 * List Streams
	 * 
	 * @return
	 */
	String[] listStreams();

	/**
	 * List Streams by kind
	 * 
	 * @param type
	 * @return
	 */
	String[] listStreams(String type);

	/**
	 * list the stream types
	 * 
	 * @return
	 */
	String[] listTypes();

	/**
	 * Open a stream
	 * 
	 * @param stream
	 *            Stream name with full name
	 * @return
	 * @throws IOException
	 */
	InputStream openStream(String stream) throws IOException;

	/**
	 * Get the stream Digest
	 * 
	 * @return
	 * @throws Exception
	 */
	String getStreamDigest(String stream) throws Exception;

	/**
	 * Save a stream to the repository
	 * 
	 * @param name
	 * @param stream
	 * @throws IOException
	 */
	void saveStream(String name, String type, InputStream stream)
			throws IOException;

	/**
	 * Delete the named stream
	 * 
	 * @param name
	 * @param type
	 * @throws IOException
	 */
	void deleteStream(String name, String type) throws IOException;

	/**
	 * this function permit to know if the underlying stream storage can save /
	 * delete stream
	 * 
	 * @return
	 */
	boolean isReadOnly();

}
