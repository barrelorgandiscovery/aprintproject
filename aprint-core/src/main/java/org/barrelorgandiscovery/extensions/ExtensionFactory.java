package org.barrelorgandiscovery.extensions;

public interface ExtensionFactory {

	/**
	 * List the extensions
	 * 
	 * @return
	 */
	IExtensionName[] listExtensionsWithoutLoading();

	/**
	 * Get the extensionname object from name
	 * 
	 * @param name
	 * @return
	 */
	IExtensionName getExtensionName(String name);

	/**
	 * Get all the extensions
	 * 
	 * @return the extension Array
	 */
	IExtension[] getExtensions();

}
