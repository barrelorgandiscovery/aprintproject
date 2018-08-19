package org.barrelorgandiscovery.playsubsystem;

import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Interface that define the play subsystem for APrint
 * 
 * @author Freydiere Patrice
 * 
 */
public interface PlaySubSystem {

	/**
	 * Launch the play of the virtual book
	 * 
	 * @param owner
	 *            the play owner
	 * @param vb
	 *            the virtual book
	 * @param feedBack
	 *            a feed back interface for user feed backs
	 * @param pos
	 *            the position for the play start (in microseconds)
	 * @throws Exception
	 */
	public PlayControl play(Object owner, VirtualBook vb,
			IPlaySubSystemFeedBack feedBack, long pos) throws Exception;

	/**
	 * Get the currently play owner
	 * 
	 * @return
	 */
	public Object getOwner();

	/**
	 * is the subsystem playing ?
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean isPlaying() throws Exception;

	/**
	 * Stop the play
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception;

}
