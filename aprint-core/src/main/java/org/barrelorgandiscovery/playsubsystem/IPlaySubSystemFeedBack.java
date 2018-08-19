package org.barrelorgandiscovery.playsubsystem;

public interface IPlaySubSystemFeedBack {

	/**
	 * Inform about the status of the play sub system
	 * 
	 * @param millis
	 * @return nano display;
	 */
	public long informCurrentPlayPosition(long millis);

	/**
	 * Inform about the abortion of the play ...
	 */
	public void playStopped();
	
	/**
	 * inform about the start of the play ...
	 */
	public void playStarted();
	

}
