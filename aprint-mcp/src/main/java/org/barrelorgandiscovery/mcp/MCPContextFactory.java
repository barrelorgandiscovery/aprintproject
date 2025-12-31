package org.barrelorgandiscovery.mcp;

import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.aprintng.APrintNGInternalFrame;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;

/**
 * Factory for creating MCP context implementations.
 * 
 * @author APrint Development Team
 */
public class MCPContextFactory {
	
	/**
	 * Create an MCP context for the given application and async jobs manager.
	 * 
	 * @param application The APrint application instance
	 * @param asyncJobsManager The async jobs manager
	 * @return An MCP context implementation
	 */
	public static APrintMCPContext createContext(APrintNGGeneralServices application, AsyncJobsManager asyncJobsManager) {
		return new APrintMCPContextImpl(application, asyncJobsManager);
	}
	
	/**
	 * Implementation of APrintMCPContext.
	 */
	private static class APrintMCPContextImpl implements APrintMCPContext {
		
		private final APrintNGGeneralServices application;
		private final AsyncJobsManager asyncJobsManager;
		
		public APrintMCPContextImpl(APrintNGGeneralServices application, AsyncJobsManager asyncJobsManager) {
			this.application = application;
			this.asyncJobsManager = asyncJobsManager;
		}
		
		@Override
		public APrintNGGeneralServices getApplication() {
			return application;
		}
		
		@Override
		public AsyncJobsManager getAsyncJobsManager() {
			return asyncJobsManager;
		}
		
		@Override
		public APrintNGVirtualBookFrame getCurrentVirtualBookFrame() {
			if (!(application instanceof APrintNG)) {
				return null;
			}
			
			APrintNG aprintNG = (APrintNG) application;
			APrintNGInternalFrame[] frames = aprintNG.listInternalFrames();
			
			// Return the first virtual book frame found (could be enhanced to return the active one)
			for (APrintNGInternalFrame frame : frames) {
				if (frame instanceof APrintNGVirtualBookFrame) {
					return (APrintNGVirtualBookFrame) frame;
				}
			}
			
			return null;
		}
		
		@Override
		public APrintGroovyConsolePanel createGroovyConsolePanel() {
			return new APrintGroovyConsolePanel();
		}
	}
}

