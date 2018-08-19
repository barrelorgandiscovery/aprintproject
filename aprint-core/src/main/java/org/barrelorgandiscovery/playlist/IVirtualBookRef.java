package org.barrelorgandiscovery.playlist;

import org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult;

public interface IVirtualBookRef {

	/**
	 * Get a name for the reference
	 * 
	 * @return
	 */
	public String getAlias();

	/**
	 * Method for knowning the reference can be opened
	 * 
	 * @return
	 */
	public boolean isValid();

	/**
	 * Open the virtual book, may take time
	 * 
	 * @return
	 * @throws Exception
	 */
	public VirtualBookResult open() throws Exception;

}