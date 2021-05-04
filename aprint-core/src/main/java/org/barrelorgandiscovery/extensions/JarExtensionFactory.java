package org.barrelorgandiscovery.extensions;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * A factory for creating Extension objects from jar files contained in a
 * folder.
 */
public class JarExtensionFactory implements ExtensionFactory {

	/** The logger. */
	private static Logger logger = Logger.getLogger(JarExtensionFactory.class);

	/** The folder. */
	private File folder;

	/**
	 * Instantiates a new jar extension factory.
	 * 
	 * @param f
	 *            the folder containing jar files. (in the jar files there are
	 *            some extensions)
	 */
	public JarExtensionFactory(File f) {
		this.folder = f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.extensions.ExtensionFactory#getExtensions()
	 */
	public IExtension[] getExtensions() {

		// liste de tous les fichiers jar du répertoire ...

		if (!folder.isDirectory() || !folder.exists())
			return new IExtension[0];

		String[] list = folder.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name != null && name.toLowerCase().endsWith(".jar"))
					return true;
				return false;
			}
		});

		ArrayList<IExtension> exts = new ArrayList<IExtension>();

		for (int i = 0; i < list.length; i++) {
			String jar = list[i];
			try {
				URLClassLoader cl = new URLClassLoader(new URL[] { new File(
						folder, jar).toURL() }, getClass().getClassLoader());

				InputStream is = cl
						.getResourceAsStream("extensions.properties");

				if (is == null) {
					logger.debug("no extensions.properties in " + jar);
					continue;
				}
				Properties p = new Properties();
				try {
					p.load(is);
				} catch (IOException ex) {
					logger.error("loading properties", ex);
					continue;
				}

				String c = p.getProperty("extensions");
				if (c == null) {
					logger.warn("no extension key in extension.properties in "
							+ jar);
					continue;
				}
				logger.debug("loading extensions " + c);

				String[] classesnames = c.split(",");
				for (int j = 0; j < classesnames.length; j++) {
					String classname = classesnames[j];
					if (classname == null)
						continue;

					classname = classname.trim();
					logger.debug("try to load " + classname);

					try {

						IExtension e = (IExtension) cl.loadClass(classname)
								.newInstance();

						exts.add(e);

					} catch (ClassNotFoundException cnfe) {
						logger.error("class " + classname + " not found");
					} catch (InstantiationException ie) {
						logger.error("cannot instanciate " + classname, ie);
					} catch (IllegalAccessException ie) {
						logger.error("error", ie);
					} catch (ClassCastException cce) {
						logger.error(
								"extension doesn't implement proper interface",
								cce);
					}

				}

			} catch (Exception ex) {
				logger.error("loading extension", ex);
			}
		}

		IExtension[] retvalue = exts.toArray(new IExtension[0]);
		return retvalue;
	}

	public IExtensionName[] listExtensionsWithoutLoading() {
		throw new RuntimeException("not implemented yet");
	}

	public IExtensionName getExtensionName(String name) {
		throw new RuntimeException("not implemented yet");
	}
}
