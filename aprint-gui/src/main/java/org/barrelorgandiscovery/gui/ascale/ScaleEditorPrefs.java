package org.barrelorgandiscovery.gui.ascale;

import java.io.File;

import org.barrelorgandiscovery.prefs.IPrefsStorage;


/**
 * Preferences
 * 
 * @author Freydiere Patrice
 * 
 */
public class ScaleEditorPrefs {

	private IPrefsStorage ps;

	private static final String DEFAULT_SCALE_FOLDER = "scales_editor_default_folder"; //$NON-NLS-1$

	public ScaleEditorPrefs(IPrefsStorage ps) {
		assert ps != null;
		this.ps = ps;
	}

	public void setLastGammeFolder(File lastscalefolder) {
		ps.setFileProperty(DEFAULT_SCALE_FOLDER, lastscalefolder);
	}

	public File getLastGammeFolder() {
		return ps.getFileProperty(DEFAULT_SCALE_FOLDER, null);
	}

}
