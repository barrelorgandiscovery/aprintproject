package org.barrelorgandiscovery.extensionsng.perfo.ng.tools;

import java.io.Serializable;

import org.barrelorgandiscovery.prefs.IPrefsStorage;

/**
 * manage the load and save of the objects
 * @author use
 *
 */
public class ClassLoaderObjectsPrefsStorage {

	private IPrefsStorage ps;

	public ClassLoaderObjectsPrefsStorage(IPrefsStorage ps) {
		this.ps = ps;
	}

	public void saveObjectProperties(String objectName, Serializable o)
			throws Exception {
		assert o != null;

		ps.setStringProperty(objectName, PunchClassloaderSerializeTools.saveBase64(o));
	}

	public Serializable loadObjectProperties(String objectName)
			throws Exception {
		return PunchClassloaderSerializeTools
				.loadBase64(ps.getStringProperty(objectName, null));
	}

}
