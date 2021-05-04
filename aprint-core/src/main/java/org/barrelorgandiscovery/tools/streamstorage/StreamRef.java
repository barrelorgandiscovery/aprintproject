package org.barrelorgandiscovery.tools.streamstorage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classe référençant un stream ...
 * 
 * @author Freydiere Patrice
 */
public class StreamRef implements IStreamRef {

	/**
	 * Référence au stream storage
	 */
	private StreamStorage sst;

	/**
	 * Nom du flux
	 */
	private String streampath;

	public StreamRef(StreamStorage sst, String streampath) {
		this.sst = sst;
		this.streampath = streampath;
	}

	public StreamStorage getInputStreamStorage() {
		return sst;
	}

	public String getStreampath() {
		return streampath;
	}

	/* (non-Javadoc)
	 * @see fr.freydierepatrice.tools.streamstorage.IStreamRef#open()
	 */
	public InputStream open() throws IOException {
		return sst.openStream(streampath);
	}

}
