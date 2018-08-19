package org.barrelorgandiscovery.extensions;

/**
 * Extension Definition (for adding external functionnalities in APrint or other
 * software)
 * 
 * @author Freydiere Patrice
 * 
 */
public interface IExtension {

	/**
	 * extension name
	 */
	String getName();

	/**
	 * Extension points
	 * 
	 * @return the array of the extension points implemented by the extension
	 */
	ExtensionPoint[] getExtensionPoints();

}
