package org.barrelorgandiscovery.prefs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;

public class AbstractFileObjectPrefsStorage extends AbstractPrefsStorage {

	private static Logger logger = Logger.getLogger(AbstractFileObjectPrefsStorage.class);

	/**
	 * Référence au fichier de propriétés
	 */
	private AbstractFileObject propertiesfile = null;

	public AbstractFileObjectPrefsStorage(AbstractFileObject propertiesfile) {
		this.propertiesfile = propertiesfile;
	}

	/**
	 * Get the file associated to the file prefs storage
	 * 
	 * @return
	 */
	public AbstractFileObject getFile() {
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
		InputStream in = propertiesfile.getInputStream();
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
		assert propertiesfile != null;
		try {
			OutputStream fos = propertiesfile.getOutputStream();
			try {

				userproperties.store(fos, Messages.getString("APrintProperties.7")); //$NON-NLS-1$

				logger.debug("saving properties to " + propertiesfile.getName().toString());
			} finally {
				fos.close();
			}
		} catch (IOException ex) {
			assert propertiesfile != null;
			logger.error("fail to save user property file " //$NON-NLS-1$
					+ propertiesfile.getName().toString(), ex);
		}
	}

}
