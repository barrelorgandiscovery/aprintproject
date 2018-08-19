package org.barrelorgandiscovery.playlist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;

/**
 * Manage a playlist storage and retrieve
 * 
 * @author pfreydiere
 * 
 */
public class PlayListManager {

	private final String PLAYLIST_STREAM_TYPE = "playlist";

	private StreamStorage ss;

	public PlayListManager(StreamStorage ss) {
		this.ss = ss;
	}

	public PlayList load(String name) throws Exception {

		InputStream s = ss.openStream(name + "" + PLAYLIST_STREAM_TYPE);
		ObjectInputStream os = new ObjectInputStream(s);
		try {
			PlayList plreadObject = (PlayList) os.readObject();

			return plreadObject;
		} finally {
			os.close();
		}
	}

	public void save(String listname, PlayList playListToSave) throws Exception {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bao);

		oos.writeObject(playListToSave);

		ss.saveStream(listname, PLAYLIST_STREAM_TYPE, new ByteArrayInputStream(
				bao.toByteArray()));

	}

	/**
	 * List playlist streams
	 */
	public String[] list() throws Exception {
		return ss.listStreams(PLAYLIST_STREAM_TYPE);
	}

}
