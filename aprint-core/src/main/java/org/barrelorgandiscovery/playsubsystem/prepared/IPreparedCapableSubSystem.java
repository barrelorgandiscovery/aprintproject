package org.barrelorgandiscovery.playsubsystem.prepared;

import org.barrelorgandiscovery.playsubsystem.IPlaySubSystemFeedBack;
import org.barrelorgandiscovery.playsubsystem.PlayControl;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * This interface inform that the play subsystem is capable of preparing the
 * play
 * 
 * @author pfreydiere
 * 
 */
public interface IPreparedCapableSubSystem {

	/**
	 * create a parameter instance for preparing the play
	 */
	ISubSystemPlayParameters createParameterInstance() throws Exception;

	/**
	 * Synchronous prepare playing method
	 * 
	 * @param transposedVirtualBook
	 * @return
	 */
	IPreparedPlaying preparePlaying(VirtualBook transposedVirtualBook,
			ISubSystemPlayParameters params) throws Exception;

	/**
	 * Play a prepared play
	 * 
	 * @param owner
	 * @param pp
	 * @param feedBack
	 * @param pos
	 */
	PlayControl playPrepared(Object owner, IPreparedPlaying pp,
			IPlaySubSystemFeedBack feedBack, long pos) throws Exception;

	/**
	 * Stop the play
	 * 
	 * @throws Exception
	 */
	void stop() throws Exception;

}
