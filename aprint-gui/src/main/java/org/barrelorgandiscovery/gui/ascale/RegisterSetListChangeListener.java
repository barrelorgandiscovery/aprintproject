package org.barrelorgandiscovery.gui.ascale;

import org.barrelorgandiscovery.scale.PipeStopGroupList;

public interface RegisterSetListChangeListener {

	/**
	 * informe que la liste des jeu de registre a chang�e ..
	 * @param newlist
	 */
	void registerSetListChanged(PipeStopGroupList newlist);
	
	
}
