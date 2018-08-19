package org.barrelorgandiscovery.extensions;

/**
 * The Interface IExtensionName define a smart naming of an extension (with a version)
 */
public interface IExtensionName {

	/**
	 * Gets the name of the extension
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Gets the version of the extension
	 * 
	 * @return the version
	 */
	String getVersion();

}
