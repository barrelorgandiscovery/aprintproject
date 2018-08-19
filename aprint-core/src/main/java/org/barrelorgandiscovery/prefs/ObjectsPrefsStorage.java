package org.barrelorgandiscovery.prefs;

import java.io.Serializable;

import org.barrelorgandiscovery.tools.SerializeTools;

/**
 * manage the load and save of the objects
 * @author use
 *
 */
public class ObjectsPrefsStorage {

	private IPrefsStorage ps;

	public ObjectsPrefsStorage(IPrefsStorage ps) {
		this.ps = ps;
	}

	public void saveObjectProperties(String objectName, Serializable o)
			throws Exception {
		assert o != null;

		ps.setStringProperty(objectName, SerializeTools.saveBase64(o));
	}

	public Serializable loadObjectProperties(String objectName)
			throws Exception {
		return SerializeTools
				.loadBase64(ps.getStringProperty(objectName, null));
	}

}
