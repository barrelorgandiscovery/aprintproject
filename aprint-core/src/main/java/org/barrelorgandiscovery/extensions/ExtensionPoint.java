package org.barrelorgandiscovery.extensions;

public interface ExtensionPoint {

	/**
	 * get the extension type id (the interface implemented by the
	 * extensionpoint)
	 * 
	 * @return the extension class implemented by this extension point
	 */
	@SuppressWarnings("unchecked")
	Class getTypeID();

	/**
	 * Reference to the interface implementing the getTypeID() type.
	 * 
	 * @return the reference of the extension point implementation
	 */
	Object getPoint();

}
