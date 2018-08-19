package org.barrelorgandiscovery.gui.ainstrument;

import org.barrelorgandiscovery.editableinstrument.InstrumentScript;

public interface ScriptsChangedListener {

	/**
	 * Signal the scripts has changed ...
	 * 
	 * @param scripts
	 */
	void scriptsChanged(InstrumentScript[] scripts);

}
