package org.barrelorgandiscovery.exec;

import java.util.Map;

/**
 * interface for executing a model or script,
 * This is an abstraction for either executing script or a modeleditor
 * 
 * @author pfreydiere
 *
 */
public interface IExecute {

	public static final String MAIN_RETURN_NAME = "return";

	/**
	 * Execute a script
	 * 
	 * @param variables
	 * @param console
	 * 
	 * @return the output variables, the main return is returned as
	 */
	public Map<String, Object> execute(Map<String, Object> variables, IConsoleLog console) throws Exception;

}
