package org.barrelorgandiscovery.gui.ainstrument;

/**
 * Listener for the modified elements in the displayer ...
 * 
 * @author Freydiere Patrice
 * 
 */
public interface WavDisplayerListener {

	void startLoopChanged(long newstart);

	void endLoopChanged(long newstart);

}
