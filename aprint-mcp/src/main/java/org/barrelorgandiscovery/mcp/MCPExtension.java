package org.barrelorgandiscovery.mcp;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InitNGExtensionPoint;

/**
 * Extension that initializes the MCP server when APrint starts.
 * This extension is loaded dynamically at runtime, breaking the circular dependency.
 * 
 * @author APrint Development Team
 */
public class MCPExtension implements IExtension, InitNGExtensionPoint {
	
	private static final Logger logger = Logger.getLogger(MCPExtension.class);
	
	private MCPServerManager mcpServerManager;
	
	@Override
	public String getName() {
		return "APrint MCP Server Extension";
	}
	
	@Override
	public ExtensionPoint[] getExtensionPoints() {
		try {
			return new ExtensionPoint[] {
				new SimpleExtensionPoint(InitNGExtensionPoint.class, this)
			};
		} catch (Exception e) {
			logger.error("Error creating extension points", e);
			return new ExtensionPoint[0];
		}
	}
	
	@Override
	public void init(APrintNG aprintNG) {
		logger.info("=== MCP Extension init() called ===");
		logger.info("Extension name: " + getName());
		
		// Check if MCP server is enabled
		String mcpEnabled = System.getProperty("aprint.mcp.enabled", "false");
		logger.info("MCP enabled system property: '" + mcpEnabled + "' (default: 'false')");
		
		if (!"true".equalsIgnoreCase(mcpEnabled)) {
			logger.warn("MCP Server is DISABLED - set system property 'aprint.mcp.enabled=true' to enable");
			logger.warn("All system properties starting with 'aprint.mcp':");
			java.util.Properties props = System.getProperties();
			for (Object key : props.keySet()) {
				String keyStr = key.toString();
				if (keyStr.startsWith("aprint.mcp")) {
					logger.warn("  " + keyStr + " = " + System.getProperty(keyStr));
				}
			}
			return;
		}
		
		logger.info("MCP Server is ENABLED - proceeding with initialization");
		
		try {
			logger.info("Initializing MCP Server Extension...");
			logger.info("APrintNG instance: " + (aprintNG != null ? "OK" : "NULL"));
			
			AsyncJobsManager asyncJobs = aprintNG != null ? aprintNG.getAsyncJobs() : null;
			logger.info("AsyncJobsManager: " + (asyncJobs != null ? "OK" : "NULL"));
			
			// Create and start server manager
			logger.info("Creating MCPServerManager...");
			mcpServerManager = new MCPServerManager(aprintNG, asyncJobs);
			logger.info("MCPServerManager created successfully");
			
			logger.info("Starting MCP Server Manager...");
			mcpServerManager.start();
			logger.info("MCP Server Manager start() called");
			
			// Wait a bit and check if server started
			Thread.sleep(500);
			if (mcpServerManager.isRunning()) {
				logger.info("MCP Server Extension initialized successfully - server is RUNNING");
				if (mcpServerManager.getHttpPort() > 0) {
					logger.info("MCP HTTP Server is listening on port: " + mcpServerManager.getHttpPort());
				}
			} else {
				logger.warn("MCP Server Manager start() returned, but server is NOT running");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Interrupted while initializing MCP Server Extension", e);
		} catch (Exception e) {
			logger.error("FAILED to initialize MCP Server Extension", e);
			logger.error("Exception type: " + e.getClass().getName());
			logger.error("Exception message: " + e.getMessage());
			if (e.getCause() != null) {
				logger.error("Caused by: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
			}
			e.printStackTrace();
		}
		logger.info("=== MCP Extension init() completed ===");
	}
}

