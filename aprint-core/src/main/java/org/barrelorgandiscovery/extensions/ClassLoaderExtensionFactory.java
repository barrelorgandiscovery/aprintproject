package org.barrelorgandiscovery.extensions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

public class ClassLoaderExtensionFactory implements ExtensionFactory {

	private static Logger logger = Logger
			.getLogger(ClassLoaderExtensionFactory.class);

	public IExtension[] getExtensions() {

		logger.debug("getExtensions");

		// read the extension propertyfile

		Properties p = new Properties();

		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"extensions.properties");

		if (is == null) {
			logger.debug("no extensions.properties");
			return new IExtension[0];
		}

		try {
			p.load(is);
		} catch (IOException ex) {
			logger.error("loading properties", ex);
			return new IExtension[0];
		}

		String c = p.getProperty("extensions");
		if (c == null)
			return new IExtension[0];

		logger.debug("loading extensions " + c);

		Vector<IExtension> v = new Vector<IExtension>();

		String[] classesnames = c.split(",");
		for (int i = 0; i < classesnames.length; i++) {
			String classname = classesnames[i];
			if (classname == null)
				continue;

			classname = classname.trim();
			logger.debug("try to load " + classname);

			try {

				IExtension e = (IExtension) Class.forName(classname)
						.newInstance();

				v.add(e);

			} catch (ClassNotFoundException cnfe) {
				logger.error("class " + classname + " not found");
			} catch (InstantiationException ie) {
				logger.error("cannot instanciate " + classname, ie);
			} catch (IllegalAccessException ie) {
				logger.error("error", ie);
			} catch (ClassCastException cce) {
				logger.error("extension doesn't implement proper interface",
						cce);
			}
		}

		IExtension[] retvalue = new IExtension[v.size()];
		v.copyInto(retvalue);

		return retvalue;
	}

	public IExtensionName[] listExtensionsWithoutLoading() {
		throw new RuntimeException("not implemented yet ...");
	}

	public IExtensionName getExtensionName(String name) {
		throw new RuntimeException("not implemented yet ...");
	}

}
