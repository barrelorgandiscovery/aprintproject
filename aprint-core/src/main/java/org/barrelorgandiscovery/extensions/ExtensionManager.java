package org.barrelorgandiscovery.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.FileTools;

/**
 * Class for managing extensions
 * 
 * @author Freydiere Patrice
 * 
 */
public class ExtensionManager implements ExtensionFactory {

	// Note, that if you load classes with your own ClassLoader, you should also
	// install your own SecurityManager (or none) by calling
	// System.setSecurityManager( null ), otherwise you might run into security
	// access violations even if you signed all your jars. Web Start's built-in
	// security manager only assigns all permissions to the classes loaded by
	// its own JNLPClassLoader.

	private static final String DEFAULT_EXTENSION_SUFFIX = ".extension";

	private static Logger logger = Logger.getLogger(ExtensionManager.class);

	private File extensionfolder = null;

	private class ExtensionName implements IExtensionName {

		private String name;
		private String version;
		private File jar;
		private String url;

		public ExtensionName(String name, String version, File jar, String url) {
			this.name = name;
			this.version = version;
			this.jar = jar;
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public String getVersion() {
			return version;
		}

		public File getJar() {
			return jar;
		}

		public String getUrl() {
			return url;
		}

		@Override
		public String toString() {
			return name + (version == null ? "" : " Version :" + version); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	public ExtensionManager(File extensionfolder) throws Exception {
		this(extensionfolder, DEFAULT_EXTENSION_PROPERTYFILE, DEFAULT_EXTENSION_SUFFIX);
	}

	private final static String DEFAULT_EXTENSION_PROPERTYFILE = "extensions.properties";

	private String extensionpropertyname = null;

	private String extension_suffix = DEFAULT_EXTENSION_SUFFIX;

	public ExtensionManager(File extensionfolder, String extensionpropertyname) throws Exception {
		this(extensionfolder, extensionpropertyname, DEFAULT_EXTENSION_SUFFIX);
	}

	public ExtensionManager(File extensionfolder, String extensionpropertyname, String jarExtension) throws Exception {

		assert jarExtension != null;
		assert !jarExtension.isEmpty();
		assert jarExtension.startsWith(".");

		this.extension_suffix = jarExtension;

		this.extensionpropertyname = extensionpropertyname;
		logger.debug("ExtensionManager init with extensionsuffixe " + extension_suffix); //$NON-NLS-1$

		if (!extensionfolder.exists() && !extensionfolder.isDirectory())
			throw new Exception("bad extension folder"); //$NON-NLS-1$

		this.extensionfolder = extensionfolder;

		// lecture des extensions ...
		String[] list = extensionfolder.list();
		if (list == null)
			return;

		for (int i = 0; i < list.length; i++) {
			String s = list[i];
			String sorigine = s;
			if (s == null)
				continue;

			if (!s.endsWith(extension_suffix)) // $NON-NLS-1$
				continue;

			s = s.substring(0, s.length() - extension_suffix.length()); // $NON-NLS-1$

			String[] token = s.split("\\_"); //$NON-NLS-1$

			String name = null;
			String version = null;

			if (token.length > 0)
				name = token[0];

			if (token.length > 2)
				version = token[1];

			if (name == null) {
				logger.debug("bad extension name"); //$NON-NLS-1$
				continue;
			}

			if (new File(extensionfolder, sorigine + ".reject").exists()) { //$NON-NLS-1$
				logger.debug("extension " + sorigine + " rejected"); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}

			logger.debug("extension found " + name + " version " + version); //$NON-NLS-1$ //$NON-NLS-2$

			// read the url associated to the extension ...

			File jarfile = new File(extensionfolder, list[i]);

			File urlfile = new File(jarfile.getAbsolutePath() + ".url"); //$NON-NLS-1$

			String url = null;

			// read the url at the origin of the url ...
			if (urlfile.exists()) {

				try {
					FileReader fr = new FileReader(urlfile);
					char[] buffer = new char[20];
					StringBuffer sb = new StringBuffer();
					int cpt;
					while ((cpt = fr.read(buffer)) != -1) {
						sb.append(buffer, 0, cpt);
					}

					url = sb.toString();

				} catch (Exception ex) {
					logger.warn("cannot read the url file associated to the extension ..."); //$NON-NLS-1$
				}
			}
			extensions.add(new ExtensionName(name, version, jarfile, url));

		}

		logger.debug("Extension manager inited with " + extensions.size() //$NON-NLS-1$
				+ " extensions"); //$NON-NLS-1$

	}

	/**
	 * extension name list
	 */
	private ArrayList<IExtensionName> extensions = new ArrayList<IExtensionName>();

	/**
	 * Download or read the extension from the URL
	 * 
	 * @param url the url of the extension
	 * @throws Exception if the read failed or an invalid url is specified
	 */
	public void downloadExtension(String url) throws Exception {

		logger.debug("downloadExtension " + url); //$NON-NLS-1$

		if (url == null)
			return;

		URL u = new URL(url); // exception if it is malformed

		String extensionname = null;

		String path = u.getPath();
		String[] t = path.split("/"); //$NON-NLS-1$
		if (t.length > 0)
			extensionname = t[t.length - 1];

		if (extensionname == null) {
			throw new Exception("no extension name"); //$NON-NLS-1$
		}

		if (extensionname.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
			extensionname = extensionname.substring(0, extensionname.length() - 4);
		}

		String name = null;
		String version = null;

		String tmp[] = extensionname.split("\\_"); //$NON-NLS-1$
		if (tmp.length > 0)
			name = tmp[0];

		if (tmp.length > 1)
			version = tmp[1];

		InputStream is = null;

		if (u.getProtocol() == null)
			throw new Exception("unsupported protocol"); //$NON-NLS-1$

		String protocol = u.getProtocol().toLowerCase();

		if ("http".equals(protocol) || "https".equals(protocol)) { //$NON-NLS-1$ //$NON-NLS-2$

			// download the file ...
			HttpClient c = new HttpClient();
			GetMethod gm = new GetMethod(url);

			logger.debug("loading the extension from " + url); //$NON-NLS-1$

			c.executeMethod(gm);

			if (gm.getStatusCode() != 200) {
				throw new Exception("Server return error " + gm.getStatusCode() //$NON-NLS-1$
						+ gm.getStatusText());
			}
			logger.debug("extension loaded, get the inputStream from source"); //$NON-NLS-1$

			is = gm.getResponseBodyAsStream();
		} else if ("file".equals(protocol)) { //$NON-NLS-1$

			String filepath = u.getPath();

			logger.debug("input :" + filepath); //$NON-NLS-1$

			is = new FileInputStream(filepath);

		} else {
			throw new Exception("unsupported protocol " + protocol); //$NON-NLS-1$
		}

		try {

			long timestamp = System.currentTimeMillis();

			String basename = "" + name + (version != null ? "_" + version : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ "_" + timestamp; //$NON-NLS-1$

			String localName = basename + ".tmp"; //$NON-NLS-1$

			String localFinalName = basename + extension_suffix; // $NON-NLS-1$

			logger.debug("local Extension file name " + localName); //$NON-NLS-1$

			logger.debug("creating outputFile"); //$NON-NLS-1$
			final File tmpfile = new File(extensionfolder, localName);
			final File finalfile = new File(extensionfolder, localFinalName);

			FileOutputStream fos = new FileOutputStream(tmpfile);
			try {

				logger.debug("reading the stream ..."); //$NON-NLS-1$

				byte[] buffer = new byte[1000];
				int cpt = -1;
				while ((cpt = is.read(buffer)) != -1) {
					fos.write(buffer, 0, cpt);
				}
			} finally {
				fos.close();
			}
			logger.debug("stream read OK"); //$NON-NLS-1$

			// rename the file

			if (!FileTools.rename(tmpfile, finalfile)) {
				throw new Exception("fail to rename file " //$NON-NLS-1$
						+ tmpfile.getAbsolutePath());
			}

			// ok

			// ecriture de l'url dans le fichier .url ...
			FileWriter fw = new FileWriter(new File(extensionfolder, localFinalName + ".url")); //$NON-NLS-1$
			fw.write(url.toString());
			fw.close();

			IExtensionName[] listJarExtensions = listJarExtensions();
			for (int i = 0; i < listJarExtensions.length; i++) {
				IExtensionName extensionName2 = listJarExtensions[i];
				if (extensionName2.getName().equals(name)) {
					invalidateExtension(extensionName2);
				}
			}

			// add the new extension ...
			extensions.add(new ExtensionName(name, version, new File(extensionfolder, localFinalName), url));
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Throwable tis) {
				}
			}
		}
	}

	/**
	 * check if the extension in managed by this ExtensionManger
	 * 
	 * @param name
	 * @return true if the extension name is managed by this extension manager
	 */
	public boolean isManaged(IExtensionName name) {
		if (name == null)
			return false;

		if (name instanceof ExtensionName)
			return true;

		return false;
	}

	/**
	 * Liste les extensions chargées
	 * 
	 * @return the found extension name array, must not be null
	 */
	public IExtensionName[] listJarExtensions() throws Exception {
		return extensions.toArray(new IExtensionName[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.extensions.ExtensionFactory#listExtensionsWithoutLoading
	 * ()
	 */
	public IExtensionName[] listExtensionsWithoutLoading() {
		return extensions.toArray(new IExtensionName[0]);
	}

	/**
	 * Invalide l'extension ...
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void invalidateExtension(IExtensionName name) throws Exception {
		if (!isManaged(name))
			throw new Exception("bad implementation"); //$NON-NLS-1$

		logger.debug("invalidate extension " + name.getName() + " version : " //$NON-NLS-1$ //$NON-NLS-2$
				+ name.getVersion());

		ExtensionName n = (ExtensionName) name;
		File jar = n.getJar();

		File rejectedjarname = new File(jar.getParent(), jar.getName() + ".reject"); //$NON-NLS-1$
		FileOutputStream fs = new FileOutputStream(rejectedjarname);
		fs.close();

		logger.debug("remove extension from list ..."); //$NON-NLS-1$
		extensions.remove(name);

	}

	/**
	 * Update the extension in reloading the plugin from original source
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void update(IExtensionName name) throws Exception {
		if (!isManaged(name))
			throw new Exception("bad implementation"); //$NON-NLS-1$
		ExtensionName n = (ExtensionName) name;

		if (n.getUrl() == null) {
			logger.error("cannot update the extension because there is no url file ..."); //$NON-NLS-1$
			throw new Exception("no url file for extension " + name.getName()); //$NON-NLS-1$
		}

		downloadExtension(n.getUrl());
	}

	public void deleteInvalidatedExtensions() throws Exception {

		// lecture des extensions ...
		String[] list = extensionfolder.list();
		if (list == null)
			return;

		for (int i = 0; i < list.length; i++) {
			String sf = list[i];
			File f = new File(extensionfolder, sf + ".reject"); //$NON-NLS-1$
			if (f.exists()) {
				logger.debug("remove extension ... " + f.getName()); //$NON-NLS-1$
				if (new File(extensionfolder, sf).delete()) {
					f.delete();
				} else {
					logger.warn("fail to delete " + sf); //$NON-NLS-1$
				}
			}
		}
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public IExtension[] getExtensions(ClassLoader classLoader) {
		IExtensionName[] listJarExtensions;
		try {
			listJarExtensions = listJarExtensions();
		} catch (Exception ex) {
			logger.error("loading Extension", ex); //$NON-NLS-1$
			return new IExtension[0];
		}

		ArrayList<IExtension> exts = new ArrayList<IExtension>();

		for (int i = 0; i < listJarExtensions.length; i++) {

			IExtensionName extensionName = listJarExtensions[i];

			ExtensionName n = (ExtensionName) extensionName;

			try {
				File currentjar = n.getJar();

				// Class theClass = Class.forName(className, true, jjcl);
				//
				// Object theInstance = theClass.newInstance();
				//

				logger.debug("loading jar " + currentjar.getAbsolutePath()); //$NON-NLS-1$

				// jcl does not implement resource loading ...

				URLClassLoader cl = new URLClassLoader(new URL[] { currentjar.toURL() }, classLoader);

				tryLoadExtension(cl, currentjar, exts);

			} catch (Exception ex) {
				logger.error("loading extension", ex); //$NON-NLS-1$
			}
		}

		try {
			logger.debug("try to load currently developed extension");
			tryLoadExtension(getClass().getClassLoader(), null, exts);

		} catch (Exception ex) {
			logger.error("loading debug extension error", ex);
		}

		logger.debug("done"); //$NON-NLS-1$
		IExtension[] retvalue = exts.toArray(new IExtension[0]);
		return retvalue;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public IExtension[] getExtensions() {
		return getExtensions(getClass().getClassLoader());
	}

	protected void tryLoadExtension(ClassLoader cl, File currentjar, List<IExtension> exts) throws Exception {

		InputStream is = null;

		if (currentjar == null) {
			is = cl.getResourceAsStream(extensionpropertyname); // $NON-NLS-1$
		} else {
			// read the file at the beginning of the jat file

			JarInputStream jis = new JarInputStream(new FileInputStream(currentjar));

			JarEntry je;
			while ((je = jis.getNextJarEntry()) != null) {
				if (je.getName().equals(extensionpropertyname)) {
					is = jis;
					break;
				}
			}

		}

		if (is == null) {
			logger.debug("no " + extensionpropertyname + " in " + currentjar); //$NON-NLS-1$
			return;
		}

		Properties p = new Properties();
		try {
			p.load(is);
		} catch (IOException ex) {
			logger.error("loading properties", ex); //$NON-NLS-1$
			return;
		}

		String c = p.getProperty("extensions"); //$NON-NLS-1$
		if (c == null) {
			logger.warn("no extension key in extension.properties in " //$NON-NLS-1$
					+ currentjar);
			return;
		}

		logger.debug("loading extensions " + c); //$NON-NLS-1$

		String[] classesnames = c.split(","); //$NON-NLS-1$
		for (int j = 0; j < classesnames.length; j++) {
			String classname = classesnames[j];
			if (classname == null)
				continue;

			classname = classname.trim();
			logger.debug("try to load " + classname); //$NON-NLS-1$

			try {

				Class toLoad = cl.loadClass(classname);
				IExtension e = (IExtension) toLoad.newInstance();
				logger.debug("loaded"); //$NON-NLS-1$
				exts.add(e);

			} catch (ClassNotFoundException cnfe) {
				logger.error("class not found while instanciating " + classname, cnfe); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (InstantiationException ie) {
				logger.error("cannot instanciate " + classname, ie); //$NON-NLS-1$
			} catch (IllegalAccessException ie) {
				logger.error("error", ie); //$NON-NLS-1$
			} catch (ClassCastException cce) {
				logger.error("extension doesn't implement proper interface", //$NON-NLS-1$
						cce);
			} catch (Throwable t) {
				t.printStackTrace(System.err);
				logger.error("error in creating extension " + classname, t); //$NON-NLS-1$
			}
		}

	}

	public IExtensionName getExtensionName(String name) {

		IExtensionName[] names = listExtensionsWithoutLoading();
		for (int i = 0; i < names.length; i++) {
			IExtensionName currentExtensionName = names[i];
			if (name.equals(currentExtensionName.getName()))
				return currentExtensionName;
		}
		return null;
	}

	// public IExtension[] getExtensions() {
	//
	// IExtensionName[] listJarExtensions;
	// try {
	// listJarExtensions = listJarExtensions();
	// } catch (Exception ex) {
	// logger.error("loading Extension", ex);
	// return new IExtension[0];
	// }
	//
	// ArrayList<IExtension> exts = new ArrayList<IExtension>();
	//
	// for (int i = 0; i < listJarExtensions.length; i++) {
	//
	// IExtensionName extensionName = listJarExtensions[i];
	//
	// ExtensionName n = (ExtensionName) extensionName;
	//
	// try {
	// File currentjar = n.getJar();
	//
	// JarClassLoader jcl = new JarClassLoader();
	//
	//
	// logger.debug("loading jar " + currentjar.getAbsolutePath());
	// jcl.add(new FileInputStream(currentjar));
	//
	// jcl.getSystemLoader().setOrder(1);
	// jcl.getLocalLoader().setOrder(2);
	// jcl.getParentLoader().setOrder(3);
	//
	// // jcl does not implement resource loading ...
	//
	// URLClassLoader cl = new URLClassLoader(new URL[] { currentjar
	// .toURL() }, getClass().getClassLoader());
	//
	// InputStream is = cl
	// .getResourceAsStream("extensions.properties");
	//
	// if (is == null) {
	// logger.debug("no extensions.properties in " + currentjar);
	// continue;
	// }
	// Properties p = new Properties();
	// try {
	// p.load(is);
	// } catch (IOException ex) {
	// logger.error("loading properties", ex);
	// continue;
	// }
	//
	// String c = p.getProperty("extensions");
	// if (c == null) {
	// logger.warn("no extension key in extension.properties in "
	// + currentjar);
	// continue;
	// }
	// logger.debug("loading extensions " + c);
	//
	// String[] classesnames = c.split(",");
	// for (int j = 0; j < classesnames.length; j++) {
	// String classname = classesnames[j];
	// if (classname == null)
	// continue;
	//
	// classname = classname.trim();
	// logger.debug("try to load " + classname);
	//
	// try {
	//
	// JclObjectFactory f = JclObjectFactory.getInstance();
	//
	// //
	// //
	//
	// IExtension e = (IExtension) f.create(jcl, classname);
	// // Object o = f.create(jcl, classname);
	// // IExtension e = JclUtils.cast(o, IExtension.class);
	//
	// exts.add(e);
	//
	// logger.debug("loaded");
	//
	// } catch (ClassNotFoundException cnfe) {
	// logger.error("class " + classname + " not found", cnfe);
	// } catch (InstantiationException ie) {
	// logger.error("cannot instanciate " + classname, ie);
	// } catch (IllegalAccessException ie) {
	// logger.error("error", ie);
	// } catch (ClassCastException cce) {
	// logger.error(
	// "extension doesn't implement proper interface",
	// cce);
	// } catch (Throwable t) {
	// logger.error("error in creating extension", t);
	// }
	//
	// }
	//
	// } catch (Exception ex) {
	// logger.error("loading extension", ex);
	// }
	// }
	//
	// logger.debug("done");
	// IExtension[] retvalue = exts.toArray(new IExtension[0]);
	// return retvalue;
	//
	// }

}
