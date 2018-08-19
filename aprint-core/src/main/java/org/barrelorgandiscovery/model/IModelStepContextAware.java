package org.barrelorgandiscovery.model;

import java.util.Map;

/**
 * all steps that need elements before execution implement this interface
 * @see ContextVariables for context variable registry
 * @author pfreydiere
 *
 */
public interface IModelStepContextAware {

	public void defineContext(Map<String,Object> context);
	
}
