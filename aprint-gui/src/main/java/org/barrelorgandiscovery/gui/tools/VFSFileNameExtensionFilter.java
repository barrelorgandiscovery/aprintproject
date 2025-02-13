package org.barrelorgandiscovery.gui.tools;

import java.util.Arrays;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;

import com.googlecode.vfsjfilechooser2.filechooser.AbstractVFSFileFilter;

/**
 * filter for filename associated to VFS objects, permit to filter the display.
 * 
 * @author pfreydiere
 */
public class VFSFileNameExtensionFilter extends AbstractVFSFileFilter {
	private static Logger logger = Logger.getLogger(VFSFileNameExtensionFilter.class);

	private String description;

	private String[] extensions = null;

	/**
	 * FileNameExtension filter
	 * 
	 * @param description the description that appear in the box
	 * @param extension   the extension (without '.')
	 */
	public VFSFileNameExtensionFilter(String description, String extension) {
		this.description = description;
		assert extension != null;
		assert !extension.startsWith(".");
		this.extensions = new String[] { extension.toLowerCase() };
	}

	/**
	 * construct the filter using a description and extensions list.
	 * 
	 * @param description
	 * @param extensions
	 */
	public VFSFileNameExtensionFilter(String description, String[] extensions) {
		this.description = description;
		this.extensions = extensions;
	}

	/*
	 * 
	 */
	@Override
	public boolean accept(FileObject f) {

		if (f == null)
			return false;

		// on accepte les répertoire
		try {
			if (f.isFolder()) {
				return true;
			}
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}

		if (!(f instanceof AbstractFileObject)) {
			return false;
		}

		AbstractFileObject a = (AbstractFileObject) f;

		for (String ext : extensions) {
			if (a.getName().getExtension().toLowerCase().endsWith(ext))
				return true;
		}
		return false;
	}

	/**
	 * get description
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * to string conversion for debug or print.
	 */
	@Override
	public String toString() {
		return "" + getDescription() + " - " + Arrays.asList(extensions);
	}

}
