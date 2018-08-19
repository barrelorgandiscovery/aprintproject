package org.barrelorgandiscovery.gui.aprintng;

/**
 * Listener Interface for the scripts
 * 
 * @author Freydiere Patrice
 * 
 */
public interface IScriptManagerListener {

	void scriptListChanged(String[] newScriptList);

	void scriptChanged(String scriptname);

}
