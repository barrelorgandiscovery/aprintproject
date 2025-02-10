package org.barrelorgandiscovery.exec;

/**
 * consol log interface, permit to log in specific console execution
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
