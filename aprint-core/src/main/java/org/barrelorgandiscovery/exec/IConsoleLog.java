package org.barrelorgandiscovery.exec;

/**
 * log object
 * 
 * @author pfreydiere
 *
 */
public interface IConsoleLog {

	/**
	 * log an object
	 * 
	 * @param objectLog
	 */
	public void log(Object objectLog);

	/**
	 * 
	 * @param string
	 */
	public void log(String string);

	/**
	 * 
	 * @param c
	 */
	public void log(char c);

}
