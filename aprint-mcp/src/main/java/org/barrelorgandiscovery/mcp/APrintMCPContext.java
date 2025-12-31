package org.barrelorgandiscovery.mcp;

import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;

/**
 * Context interface for MCP server to access APrint application services.
 * This provides a clean interface for the MCP server to interact with
 * the application without tight coupling.
 * 
 * @author APrint Development Team
 */
public interface APrintMCPContext {
	
	/**
	 * Get the main APrint application instance
	 */
	APrintNGGeneralServices getApplication();
	
	/**
	 * Get the async jobs manager
	 */
	AsyncJobsManager getAsyncJobsManager();
	
	/**
	 * Get the current active virtual book frame, if any
	 */
	APrintNGVirtualBookFrame getCurrentVirtualBookFrame();
	
	/**
	 * Create a Groovy console panel for script execution
	 */
	APrintGroovyConsolePanel createGroovyConsolePanel();
}

