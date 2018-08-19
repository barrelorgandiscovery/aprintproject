package org.barrelorgandiscovery.playlist;

import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.VirtualBookMetadata;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult;

/**
 * Serializable Reference of a virtual book
 * 
 * @author use
 * 
 */
public class VirtualBookRef implements Serializable, IVirtualBookRef {

	private static Logger logger = Logger.getLogger(VirtualBookRef.class);

	private File ref = null;

	private boolean isValid;

	private String alias = "<unknown>";

	/**
	 * Construct a VirtualBookReference, and read the metadata informations
	 * 
	 * @param refFile
	 *            the ref file
	 * @throws Exception
	 */
	public VirtualBookRef(File refFile) throws Exception {
		if (refFile == null)
			throw new Exception("null file ref given");

		// read metadata information
		// could be accelerated
		if (refFile.exists()) {
			try {
				VirtualBookResult r = VirtualBookXmlIO.read(refFile);
				VirtualBookMetadata mt = r.virtualBook.getMetadata();
				if (mt != null) {
					alias = r.virtualBook.getName();
					if (alias == null || "".equals(alias)) {
						alias = "" + refFile.getName();
					}

					isValid = true;
					this.ref = refFile;
					return;

				}
			} catch (Exception ex) {
				logger.error(
						"error reading " + refFile + " " + ex.getMessage(), ex);
			}

		}

		isValid = false;

	}

	/* (non-Javadoc)
	 * @see org.barrelorgandiscovery.playlist.IVirtualBookRef#getAlias()
	 */
	public String getAlias() {
		return this.alias;
	}

	/* (non-Javadoc)
	 * @see org.barrelorgandiscovery.playlist.IVirtualBookRef#isValid()
	 */
	public boolean isValid() {
		return this.isValid;
	}

	/* (non-Javadoc)
	 * @see org.barrelorgandiscovery.playlist.IVirtualBookRef#open()
	 */
	public VirtualBookResult open() throws Exception {
		return VirtualBookXmlIO.read(ref);
	}

}
