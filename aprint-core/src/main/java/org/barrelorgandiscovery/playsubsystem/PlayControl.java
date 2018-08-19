package org.barrelorgandiscovery.playsubsystem;

/**
 * object permit to control the play
 *
 * @author pfreydiere
 */
public interface PlayControl {

	/**
	 * get tempo factor
	 * @return
	 */
	public float getTempo();

	/**
	 * set tempo factor
	 * @param newTempo
	 */
	public void setTempo(float newTempo);

}
