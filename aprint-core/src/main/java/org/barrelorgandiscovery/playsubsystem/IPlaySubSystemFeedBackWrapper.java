package org.barrelorgandiscovery.playsubsystem;

/**
 * Wrapper that handle the 2 versions of the interfaces
 * 
 * @author use
 * 
 */
class IPlaySubSystemFeedBackWrapper implements IPlaySubSystemFeedBack2 {

	private IPlaySubSystemFeedBack iPlaySubSystemFeedBack = null;

	private IPlaySubSystemFeedBack2 iPlaySubSystemFeedBack2 = null;

	/**
	 * Construct
	 * 
	 * @param feedBack
	 *            the feedback interface that could implement 2 version of the
	 *            interface, feedback may also be null
	 */
	public IPlaySubSystemFeedBackWrapper(IPlaySubSystemFeedBack feedBack) {
		this.iPlaySubSystemFeedBack = feedBack;
		if (feedBack instanceof IPlaySubSystemFeedBack2) {
			iPlaySubSystemFeedBack2 = (IPlaySubSystemFeedBack2) feedBack;
		}
	}

	public long informCurrentPlayPosition(long millis) {
		if (iPlaySubSystemFeedBack != null)
			return iPlaySubSystemFeedBack.informCurrentPlayPosition(millis);
		return 0;
	}

	public void playStopped() {
		if (iPlaySubSystemFeedBack != null)
			iPlaySubSystemFeedBack.playStopped();

	}

	public void playStarted() {
		if (iPlaySubSystemFeedBack != null)
			iPlaySubSystemFeedBack.playStarted();
	}

	public void playFinished() {
		if (iPlaySubSystemFeedBack2 != null)
			iPlaySubSystemFeedBack2.playFinished();
	}

}
