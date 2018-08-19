package org.barrelorgandiscovery.gui.ainstrument;

public interface WavPlayerListener {

	
	void startPlaying();
	
	void playStateChanged(long pos);
	
	void playStopped();
	
	
}
