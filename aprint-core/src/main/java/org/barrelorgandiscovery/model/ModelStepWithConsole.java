package org.barrelorgandiscovery.model;

import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.script.groovy.IScriptConsole;

public abstract class ModelStepWithConsole extends ModelStep implements IModelStepContextAware{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1868056203496235053L;

	private static final Logger logger = Logger.getLogger(ModelStepWithConsole.class);
		
	protected transient IScriptConsole console;

	@Override
	public void defineContext(Map<String, Object> context) {
		Object o = context.get(ContextVariables.CONTEXT_CONSOLE);
		if (o != null && (o instanceof IScriptConsole)) {
			this.console = (IScriptConsole) o;
		}
	}

	protected void log(String message) {
		if (console != null) {
			try {
				console.appendOutputNl(message, null);
			} catch (Exception ex) {
				logger.debug(ex);
			}
		}
	}
	
	protected void log(Exception ex) {
		if (console != null) {
			try {
				console.appendOutput(ex);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
}
