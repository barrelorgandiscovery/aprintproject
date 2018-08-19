package org.barrelorgandiscovery.exec;

import java.util.Map;

/**
 * interface for executing a model or script, 
 * this unify transforms done using a script or a model
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
