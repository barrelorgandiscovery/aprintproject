package org.barrelorgandiscovery.gui.aprintng;

import org.barrelorgandiscovery.playsubsystem.PlaySubSystem;

public interface IPlaySubSystemManager {

	/**
	 * Get the current play sub system
	 * 
	 * @return
	 */
	PlaySubSystem getCurrent();

	void addPlaySubSystemManagerListener(IPlaySubSystemManagerListener listener);

	void removePlaySubSystemManagerListener(
			IPlaySubSystemManagerListener listener);

}
