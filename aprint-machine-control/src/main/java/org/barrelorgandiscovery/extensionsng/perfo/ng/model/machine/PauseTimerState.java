package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

import org.barrelorgandiscovery.tools.Disposable;

/**
 * simple state machine for pauses, applied to for punch or lazer
 * 
 * @author pfreydiere
 * 
 */
public class PauseTimerState {

	/**
	 * configure pause every XXX milliseconds
	 */
	long configuredPauseIntervalInMs = -1;

	/**
	 * pause time in ms
	 */
	long configuredPauseTimeInMs = -1;

	public PauseTimerState() {
	}

	public void setConfiguredPauseIntervalInMs(long configuredPauseIntervalInMs) {
		assert configuredPauseIntervalInMs > 10_000;
		this.configuredPauseIntervalInMs = configuredPauseIntervalInMs;
	}

	public void setConfiguredPauseTimeInMs(long configuredPauseTimeInMs) {
		assert configuredPauseTimeInMs > 10_000;
		this.configuredPauseTimeInMs = configuredPauseTimeInMs;
	}

	public long getConfiguredPauseIntervalInMs() {
		return configuredPauseIntervalInMs;
	}

	public long getConfiguredPauseTimeInMs() {
		return configuredPauseTimeInMs;
	}

	Long startedTime = null;

	Long nextPauseTime = null;
	Long exitedPauseTime = null;

	public void startPunch() {
		this.startedTime = System.currentTimeMillis();
		this.nextPauseTime = this.startedTime + this.configuredPauseIntervalInMs;
		this.exitedPauseTime = this.nextPauseTime + this.configuredPauseTimeInMs;
	}

	public void stopPunch() {
		this.startedTime = null;
		this.nextPauseTime = null;
		this.exitedPauseTime = null;
	}

	public boolean isInPause() {

		if (startedTime == null || nextPauseTime == null || exitedPauseTime == null) {
			return false;
		}

		assert startedTime != null;
		assert nextPauseTime != null;

		long time = System.currentTimeMillis();
		if (time < nextPauseTime) {
			return false;
		}

		// in pause time

		if (time < exitedPauseTime) {
			// still in pause
			return true;
		}

		// exited pause time passed
		assert exitedPauseTime != null;
		nextPauseTime = exitedPauseTime + this.configuredPauseIntervalInMs;
		this.exitedPauseTime = this.nextPauseTime + this.configuredPauseTimeInMs;

		// reevaluate
		return isInPause();
	}

}
