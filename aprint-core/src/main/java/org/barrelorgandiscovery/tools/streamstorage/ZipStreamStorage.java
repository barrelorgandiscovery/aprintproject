package org.barrelorgandiscovery.tools.streamstorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.util.Base64;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Stream storage that uncompress on demand a specific stream
 * 
 * for rewind capabilities, the hole zip file is remembered in memory, this is
 * too slow this is good for a small subset
 * 
 * @author Freydiere Patrice
 * 
 */
public class ZipStreamStorage implements StreamStorage {

	private static final Logger logger = Logger
			.getLogger(ZipStreamStorage.class);

	private byte[] zipcontent;

	/**
	 * Constructeur, prend en paramètre un flux contenant le flux zippé ce flux
	 * est chargé complètement dans l'outil. Attention aux flux volumineux
	 * 
	 * @param inputStream
	 *            le flux d'entrée
	 * @throws IOException
	 */
	public ZipStreamStorage(InputStream inputStream) throws IOException {

		if (inputStream == null)
			throw new IllegalArgumentException("input stream is null");

		// read the stream in memory
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);

		byte[] buffer = new byte[4096];

		int cpt;

		do {
			cpt = inputStream.read(buffer);
			if (cpt == -1)
				break;
			// logger.debug("read " + cpt + " elements ");
			baos.write(buffer, 0, cpt);
		} while (true);

		baos.close();

		zipcontent = baos.toByteArray();

		logger.debug("zip content read, length " + zipcontent.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.utils.InputStreamStorage#listStreams()
	 */
	public String[] listStreams() {

		Vector<String> v = new Vector<String>();

		ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(
				zipcontent));
		try {

			ZipEntry ze;
			while ((ze = z.getNextEntry()) != null) {
				final String name = ze.getName();
				v.add(name);
			}

		} catch (IOException ex) {
			logger.error("listStreams", ex);
		}

		String[] retvalue = new String[v.size()];
		v.copyInto(retvalue);
		return retvalue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.utils.InputStreamStorage#listStreams(java.lang.String)
	 */
	public String[] listStreams(String type) {
		Vector<String> v = new Vector<String>();

		ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(
				zipcontent));
		try {

			ZipEntry ze;
			while ((ze = z.getNextEntry()) != null) {
				String name = ze.getName();
				if (name.endsWith("." + type))
					v.add(name);
			}

		} catch (IOException ex) {
			logger.error("listStreams", ex);
		}

		String[] retvalue = new String[v.size()];
		v.copyInto(retvalue);
		return retvalue;
	}

	public String[] listTypes() {

		Set<String> types = new TreeSet<String>();

		ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(
				zipcontent));
		try {

			ZipEntry ze;
			while ((ze = z.getNextEntry()) != null) {
				String name = ze.getName();
				int pos = name.lastIndexOf('.');
				if (pos != -1) {
					types.add(name.substring(pos + 1));
				}

			}

		} catch (IOException ex) {
			logger.error("listStreams", ex);
		}

		return types.toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.utils.StreamStorage#openStream(java.lang.String)
	 */
	public InputStream openStream(String stream) throws IOException {
		ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(
				zipcontent));
		try {

			ZipEntry ze;
			while ((ze = z.getNextEntry()) != null) {
				String name = ze.getName();
				if (name.equals(stream)) {
					// lecture du flux dans un tableau et réécriture dans un
					// byte array pour permettre l'utilisation du "reset"

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[4096];
					int cpt;
					while ((cpt = z.read(buffer)) != -1) {
						baos.write(buffer, 0, cpt);
					}

					return new ByteArrayInputStream(baos.toByteArray());
				}
			}
		} catch (IOException ex) {
			logger.error("listStreams", ex);
		}

		throw new IOException("stream " + stream + " not found");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.streamstorage.StreamStorage#getStreamDigest()
	 */
	public String getStreamDigest(String stream) throws Exception {

		ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(
				zipcontent));
		try {

			ZipEntry ze;
			while ((ze = z.getNextEntry()) != null) {
				String name = ze.getName();
				if (name.equals(stream)) {
					// lecture du flux dans un tableau et réécriture dans un
					// byte array pour permettre l'utilisation du "reset"

					if (ze.getCrc() != -1)
						return "" + ze.getCrc();

					logger.debug("crc not found, compute the digest");

					MessageDigest md = MessageDigest.getInstance("SHA1");
					md.reset();

					byte[] buffer = new byte[4096];
					int cpt;
					while ((cpt = z.read(buffer)) != -1) {
						md.update(buffer, 0, cpt);
					}

					return new String(Base64.encode(md.digest()), "UTF-8");
				}
			}
		} catch (IOException ex) {
			logger.error("listStreams", ex);
		}

		throw new IOException("stream " + stream + " not found");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.StreamStorage#saveStream(java.lang.String,
	 *      java.lang.String, java.io.InputStream)
	 */
	public void saveStream(String name, String type, InputStream stream)
			throws IOException {

		throw new NotImplementedException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.StreamStorage#deleteStream(java.lang.String,
	 *      java.lang.String)
	 */
	public void deleteStream(String name, String type) throws IOException {
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.StreamStorage#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

}
