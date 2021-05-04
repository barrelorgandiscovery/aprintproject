package org.barrelorgandiscovery.tools;

import java.util.Arrays;

import javax.swing.filechooser.FileFilter;

public class FileNameExtensionFilter extends FileFilter {

	private String description;

	private String[] extensions = null;

	/**
	 * FileNameExtension filter
	 * 
	 * @param description the description that appear in the box
	 * @param extension   the extension (without '.')
	 */
	public FileNameExtensionFilter(String description, String extension) {
		this.description = description;

		this.extensions = new String[] { extension.toLowerCase() };
	}

	public FileNameExtensionFilter(String description, String[] extensions) {
		this.description = description;
		this.extensions = extensions;
	}

	@Override
	public boolean accept(java.io.File f) {

		if (f == null)
			return false;

		// on accepte les répertoire

		if (f.isDirectory())
			return true;

		for (String ext : extensions) {
			if (f.getName().toLowerCase().endsWith("." + ext))
				return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "" + getDescription() + " - " + Arrays.asList(extensions);
	}

}
