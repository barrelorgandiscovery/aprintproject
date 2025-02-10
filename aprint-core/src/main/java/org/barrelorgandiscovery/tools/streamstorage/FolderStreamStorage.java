package org.barrelorgandiscovery.tools.streamstorage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.Provider;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.Base64Tools;
import org.barrelorgandiscovery.tools.FileTools;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.barrelorgandiscovery.tools.StringTools;

/**
 * Implémentation d'un stockage sous forme de répertoire
 * 
 * @author Freydiere Patrice
 * 
 */
public class FolderStreamStorage implements StreamStorage {

	private static final String STREAMDIGEST_FILE_SUFFIX = ".sha1";

	private static Logger logger = Logger.getLogger(FolderStreamStorage.class);

	private File folder;

	public FolderStreamStorage(File folder) throws Exception {
		if (!folder.isDirectory() || !folder.exists())
			throw new Exception("folder " + folder.getAbsolutePath()
					+ " does not exist");
		this.folder = folder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.StreamStorage#listStreams()
	 */
	public String[] listStreams() {

		File[] files = folder.listFiles();

		Vector<String> tmp = new Vector<String>();
		for (int i = 0; i < files.length; i++) {
			tmp.add(files[i].getName());
		}

		String[] r = new String[tmp.size()];
		tmp.copyInto(r);
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.StreamStorage#openStream(java.lang.String)
	 */
	public InputStream openStream(String stream) throws IOException {

		BufferedInputStream s = new BufferedInputStream(new FileInputStream(
				new File(folder, stream)));
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				byte[] buffer = new byte[4096];
				int cpt;
				while ((cpt = s.read(buffer)) != -1) {
					baos.write(buffer, 0, cpt);
				}
				return new ByteArrayInputStream(baos.toByteArray());
			} finally {
				baos.close();
			}

		} finally {
			s.close();
		}
	}

	private void removeStreamDigestCache(File f) {
		if (f == null)
			return;

		if (!f.exists())
			return;

		if (f.isDirectory()) {
			File[] subfiles = f.listFiles();
			for (int i = 0; i < subfiles.length; i++) {
				File file = subfiles[i];
				removeStreamDigestCache(file);
			}
		} else {
			if (f.getName().endsWith(STREAMDIGEST_FILE_SUFFIX)) {
				f.delete();
			}
		}

	}

	public void removeAllStreamDigestCaches() throws Exception {
		logger.debug("remove All Stream Digest Caches");
		removeStreamDigestCache(folder);
	}

	public String getStreamDigest(String stream) throws Exception {

		File ref = new File(folder, stream);
		if (!ref.exists())
			throw new Exception("stream " + stream + " not found");

		File rSha1 = new File(ref.getAbsolutePath() + STREAMDIGEST_FILE_SUFFIX);
		if (rSha1.exists()) {
			FileInputStream fis = new FileInputStream(rSha1);
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				StreamsTools.copyStream(fis, baos);

				byte[] sha1 = baos.toByteArray();
				return new String(sha1, "UTF-8");

			} finally {
				fis.close();
			}
		}

		logger.debug("sha not found, compute it ...");
		// compute SHA1 ...
		InputStream os = openStream(stream);
		BufferedInputStream bos = new BufferedInputStream(os);
		try {

			MessageDigest md = MessageDigest.getInstance("SHA1");
			
			md.reset();

			byte[] c = new byte[10000];
			int cpt;
			while ((cpt = bos.read(c)) != -1) {
				md.update(c, 0, cpt);
			}

			byte[] digestbytes = md.digest();
			byte[] digest = Base64Tools.encodeToBytes(digestbytes);

			try {

				FileOutputStream fos = new FileOutputStream(rSha1);
				fos.write(digest);
				fos.close();

			} catch (Exception ex) {
				logger.warn("cannot write sha digest ...", ex);
			}

			return new String(digest, "UTF-8");
		} finally {
			bos.close();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.tools.StreamStorage#listStreams(java.lang.String)
	 */
	public String[] listStreams(String type) {
		File[] files = folder.listFiles();

		Vector<String> tmp = new Vector<String>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("." + type))
				tmp.add(files[i].getName());
		}

		String[] r = new String[tmp.size()];
		tmp.copyInto(r);
		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.barrelorgandiscovery.tools.streamstorage.StreamStorage#listTypes()
	 */
	public String[] listTypes() {
		File[] files = folder.listFiles();
		Set<String> tmp = new TreeSet<String>();
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();
			int typepos = fileName.lastIndexOf(".");
			if (typepos != -1) {
				String type = fileName.substring(typepos + 1);
				tmp.add(type);
			}
		}
		return tmp.toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.StreamStorage#saveStream(java.lang.String,
	 * java.lang.String, java.io.InputStream)
	 */
	public void saveStream(String name, String type, InputStream stream)
			throws IOException {

		if (name == null)
			throw new InvalidParameterException("name cannot be null");

		if (!name.equalsIgnoreCase(StringTools.convertToPhysicalName(name)))
			throw new IOException("invalid stream name :" + name);

		String streamname = name;
		if (type != null)
			streamname += "." + type;

		logger.debug("saving stream " + streamname);

		// création d'un fichier temporaire ...
		File temp = File.createTempFile("tmp", "tmp");
		FileOutputStream fos = new FileOutputStream(temp);
		try {

			byte[] buffer = new byte[4096];
			int cpt;
			while ((cpt = stream.read(buffer)) != -1) {
				fos.write(buffer, 0, cpt);
			}
			fos.close();
			fos = null;

			logger.debug("temp stream saved");

			File destfile = new File(folder, streamname);
			File destfilebackup = null;

			if (destfile.exists()) {

				// suppression du fichier sha1
				new File(destfile.getAbsolutePath() + STREAMDIGEST_FILE_SUFFIX)
						.delete();

				logger.debug("dest stream exist, rename to .bak");

				destfilebackup = new File(destfile.getParentFile(),
						destfile.getName() + ".bak");

				if (destfilebackup.exists()) {
					logger.debug("delete backup file ...");
					destfilebackup.delete();
				}
				if (!FileTools.rename(destfile, destfilebackup))
					throw new IOException("fail to rename to backup file");
			}
			
			// invariant, le fichier de destination est renommé en .bak

			if (!FileTools.rename(temp, destfile)) {

				logger.error("fail to rename " + temp.getAbsolutePath()
						+ " to " + destfile.getAbsolutePath());

				if (destfilebackup != null) {
					logger.warn("rename the backup to the last one (" + destfilebackup.getAbsolutePath() + " to " + destfile.getAbsolutePath() + ")"); //$NON-NLS-1$
					FileTools.rename(destfilebackup, destfile);
				}

				throw new IOException("fail to rename "
						+ temp.getAbsolutePath());
			}

		} finally {
			if (fos != null)
				fos.close();
			temp.delete();
		}

		try {
			// compute the digest ...
			getStreamDigest(streamname);
		} catch (Exception e) {
			logger.warn("cannot update stream digest " + streamname, e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.tools.StreamStorage#deleteStream(java.lang.String,
	 * java.lang.String)
	 */
	public void deleteStream(String name, String type) throws IOException {
		if (name == null)
			throw new InvalidParameterException("name cannot be null");

		String streamname = name;
		if (type != null)
			streamname += "." + type;

		logger.debug("remove stream " + streamname);

		File file = new File(folder, streamname);
		if (!file.delete())
			throw new IOException("fail to delete " + file.getAbsolutePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.StreamStorage#isReadOnly()
	 */
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * Get the stored folder
	 * 
	 * @return
	 */
	public File getFolder() {
		return this.folder;
	}

}
