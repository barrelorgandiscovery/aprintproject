package org.barrelorgandiscovery.tools;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * Do nothing logger, used when a class is multithreaded and not launched in the
 * UI thread, that cause the LF5 to fail or hang
 * 
 * @author use
 * 
 */
public class FakeLogger extends Logger {

	public FakeLogger(String name) {
		super(name);
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public boolean isEnabledFor(Priority level) {
		return false;
	}

	@Override
	public boolean isInfoEnabled() {
		return false;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}
	
	@Override
	public void trace(Object message, Throwable t) {

	}

	@Override
	public void trace(Object message) {

	}

	@Override
	public void debug(Object message, Throwable t) {

	}

	@Override
	public void debug(Object message) {

	}

	@Override
	public void error(Object message, Throwable t) {

	}

	@Override
	public void error(Object message) {

	}

	@Override
	public void fatal(Object message, Throwable t) {

	}

	@Override
	public void fatal(Object message) {

	}

	@Override
	public void info(Object message, Throwable t) {

	}

	@Override
	public void info(Object message) {

	}

	@Override
	public void warn(Object message, Throwable t) {

	}

	@Override
	public void warn(Object message) {

	}

}
