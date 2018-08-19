package org.barrelorgandiscovery.extensions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import fr.pfreydiere.extensions.extensions.Configuration;
import fr.pfreydiere.extensions.extensions.ConfigurationDocument;
import fr.pfreydiere.extensions.extensions.ExtensionRef;
import fr.pfreydiere.extensions.extensions.ExtensionRefList;

/**
 * This class manage a repository of extensions, by reading an xml containing
 * all the extension in the repository
 * 
 * @author Freydiere Patrice
 * 
 */
public class ExtensionRepository {

	private static Logger logger = Logger.getLogger(ExtensionRepository.class);

	private ExtensionRef[] extensionList;

	private URL url;

	public ExtensionRepository(URL url) throws Exception {
		// récupération du fichier XML ....

		ConfigurationDocument configurationdocument = ConfigurationDocument.Factory
				.parse(url.openStream());

		if (configurationdocument == null)
			throw new Exception("no configurationdocument in the source");

		Configuration configuration = configurationdocument.getConfiguration();
		if (configuration == null)
			throw new Exception("no configuration element in the document");

		ExtensionRefList extensions = configuration.getExtensions();
		if (extensions == null)
			throw new Exception("no extensions in repository");

		extensionList = extensions.getExtensionRefArray();

		this.url = url;

	}

	/**
	 * Get the URL of the repository
	 * 
	 * @return
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * This function load the repository description
	 */
	public ExtensionRef[] listExtensions() throws Exception {
		return extensionList;
	}

	/**
	 * Find an Extension Ref by it's name ...
	 * 
	 * @param extensionname
	 *            the name of the extension ...
	 * @return
	 */
	private ExtensionRef findExtensionByName(String extensionname) {
		assert extensionname != null;

		if (extensionList == null)
			return null;

		logger.debug("finding extension " + extensionname);
		
		for (int i = 0; i < extensionList.length; i++) {
			ExtensionRef currentExtensionRef = extensionList[i];
			String url2 = currentExtensionRef.getUrl();

			try {
				URL url3 = new URL(url2);
				logger.debug("url : " + url3.toString());
				
				File f = new File(url3.getPath());
				
				if (f.getName().startsWith(extensionname + "."))
				{
					logger.debug("extension found ...");
					return currentExtensionRef;
				}
			} catch (MalformedURLException ex) {
				logger.error(ex.getMessage(), ex);
			}

		}
		return null;
	}

	/**
	 * List all extension that are not installed
	 * 
	 * @param manager
	 * @return
	 * @throws Exception
	 */
	public ExtensionRef[] listExtensionInstalled(ExtensionManager manager)
			throws Exception {
		IExtensionName[] names = manager.listExtensionsWithoutLoading();

		ArrayList<ExtensionRef> retvalue = new ArrayList<ExtensionRef>();

		for (int i = 0; i < names.length; i++) {
			IExtensionName extensionName = names[i];
			String thename = extensionName.getName();

			ExtensionRef foundExtensionRef = findExtensionByName(thename);

			if (foundExtensionRef != null)
				retvalue.add(foundExtensionRef);
		}

		return retvalue.toArray(new ExtensionRef[0]);
	}

	/**
	 * List the extension that should be updated ...
	 * 
	 * @param manager
	 * @return
	 */
	public ExtensionRef[] listExtensionsThatShouldBeUpdated(
			ExtensionManager manager) {
		IExtensionName[] names = manager.listExtensionsWithoutLoading();

		ArrayList<ExtensionRef> retvalue = new ArrayList<ExtensionRef>();

		for (int i = 0; i < names.length; i++) {
			IExtensionName extensionName = names[i];
			String thename = extensionName.getName();

			ExtensionRef foundExtensionRef = findExtensionByName(thename);

			if (foundExtensionRef != null) {
				logger.debug("extension " + thename
						+ " found in the repository, check the version ...");

				String repositoryextensionversion = foundExtensionRef
						.getVersion();
				if (repositoryextensionversion != null) {
					if (!repositoryextensionversion.equals(extensionName
							.getVersion())) {
						logger.debug("extension has local version "
								+ extensionName.getVersion()
								+ " and the repository is "
								+ repositoryextensionversion
								+ " this extension should be updated");
						retvalue.add(foundExtensionRef);
					}
				} else {
					logger
							.debug("extension has no version .. cannot check the version ...");
				}

			}
		}

		return retvalue.toArray(new ExtensionRef[0]);
	}

}
