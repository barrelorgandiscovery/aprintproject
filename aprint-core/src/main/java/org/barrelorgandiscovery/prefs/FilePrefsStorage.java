package org.barrelorgandiscovery.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;

public class FilePrefsStorage extends AbstractPrefsStorage {

	private static Logger logger = Logger.getLogger(FilePrefsStorage.class);

	/**
	 * Référence au fichier de propriétés
	 */
	private File propertiesfile = null;

	public FilePrefsStorage(File propertiesfile) {
		this.propertiesfile = propertiesfile;
	}

	/**
	 * Get the file associated to the file prefs storage
	 * 
	 * @return
	 */
	public File getFile() {
		return propertiesfile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.IPrefsStorage#load()
	 */
	public void load() throws IOException {

		if (!propertiesfile.exists()) {
			logger.debug("creating new properties");
			userproperties = new Properties();
			return;
		}

		logger.debug("reading properties");
		// Lecture du fichier ..
		InputStream in = new FileInputStream(propertiesfile);
		try {
			userproperties.load(in);
		} finally {
			in.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.IPrefsStorage#save()
	 */
	public void save() {

		try {
			FileOutputStream fos = new FileOutputStream(propertiesfile);
			try {

				userproperties.store(fos,
						Messages.getString("APrintProperties.7")); //$NON-NLS-1$

				logger.debug("saving properties to "
						+ propertiesfile.getAbsolutePath());
			} finally {
				fos.close();
			}
		} catch (IOException ex) {
			assert propertiesfile != null;
			logger.error("fail to save user property file " //$NON-NLS-1$
					+ propertiesfile.getAbsolutePath(), ex);
		}
	}

}
