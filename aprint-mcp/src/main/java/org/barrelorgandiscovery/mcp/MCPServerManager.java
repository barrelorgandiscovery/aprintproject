package org.barrelorgandiscovery.mcp;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.mcp.tools.ExecuteGroovyScriptTool;
import org.barrelorgandiscovery.mcp.tools.GetVirtualBookInfoTool;
import org.barrelorgandiscovery.mcp.tools.ListVirtualBookFramesTool;
import org.barrelorgandiscovery.mcp.tools.TriggerPlayTool;
import org.barrelorgandiscovery.mcp.tools.TriggerStopTool;
import org.barrelorgandiscovery.mcp.transport.HttpServerSseTransportProvider;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Manager for the MCP server using the official MCP Java SDK.
 * Handles starting and stopping the server in a separate thread so it doesn't block the main application.
 * 
 * The server runs in HTTP mode with SSE transport.
 * 
 * @author APrint Development Team
 */
public class MCPServerManager {
	
	private static final Logger logger = Logger.getLogger(MCPServerManager.class);
	
	private HttpServerSseTransportProvider transportProvider;
	private McpSyncServer mcpServer;
	private Thread serverThread;
	private final APrintNGGeneralServices application;
	private final AsyncJobsManager asyncJobsManager;
	private final int httpPort;
	
	public MCPServerManager(APrintNGGeneralServices application, AsyncJobsManager asyncJobsManager) {
		logger.info("=== MCPServerManager constructor (SDK-based) ===");
		logger.info("Application: " + (application != null ? application.getClass().getName() : "NULL"));
		logger.info("AsyncJobsManager: " + (asyncJobsManager != null ? "OK" : "NULL"));
		
		this.application = application;
		this.asyncJobsManager = asyncJobsManager;
		
		// HTTP mode is always used with SDK
		logger.info("Using HTTP mode with MCP SDK");
		
		// Get HTTP port from system property (default: 9090)
		String portStr = System.getProperty("aprint.mcp.port", "9090");
		logger.info("MCP port system property: '" + portStr + "' (default: '9090')");
		int port = 9090;
		try {
			port = Integer.parseInt(portStr);
			logger.info("Parsed port: " + port);
		} catch (NumberFormatException e) {
			logger.warn("Invalid port number: " + portStr + ", using default 9090", e);
		}
		this.httpPort = port;
		logger.info("Final HTTP port: " + this.httpPort);
		logger.info("=== MCPServerManager constructor completed ===");
	}
	
	/**
	 * Start the MCP server in a separate thread.
	 * Uses HTTP mode with SSE transport.
	 */
	public void start() {
		logger.info("=== MCPServerManager.start() called ===");
		
		if (mcpServer != null || transportProvider != null) {
			logger.warn("MCP Server is already running - skipping start");
			return;
		}
		
		logger.info("Starting MCP Server Manager (SDK-based)...");
		logger.info("Application: " + (application != null ? "OK" : "NULL"));
		logger.info("AsyncJobsManager: " + (asyncJobsManager != null ? "OK" : "NULL"));
		
		try {
			// Create MCP context
			logger.info("Creating MCP context using factory...");
			APrintMCPContext context = MCPContextFactory.createContext(application, asyncJobsManager);
			logger.info("MCP context created: " + (context != null ? "OK" : "NULL"));
			
			// Start server in a separate thread
			logger.info("Creating server thread...");
			serverThread = new Thread(() -> {
				logger.info("=== MCP Server Thread Started (SDK-based) ===");
				try {
					logger.info("Starting MCP HTTP Server on port " + httpPort);
					
					// Create transport provider
					logger.info("Creating HttpServerSseTransportProvider...");
					transportProvider = new HttpServerSseTransportProvider(httpPort);
					logger.info("Transport provider created");
					
					// Build MCP server FIRST - this sets the session factory on the transport provider
					logger.info("Registering tools with SDK...");
					mcpServer = McpServer.sync(transportProvider)
						.serverInfo("aprint-mcp-server", "1.0.0")
						.toolCall(ExecuteGroovyScriptTool.createTool(), ExecuteGroovyScriptTool.createHandler(context))
						.toolCall(GetVirtualBookInfoTool.createTool(), GetVirtualBookInfoTool.createHandler(context))
						.toolCall(TriggerPlayTool.createTool(), TriggerPlayTool.createHandler(context))
						.toolCall(TriggerStopTool.createTool(), TriggerStopTool.createHandler(context))
						.toolCall(ListVirtualBookFramesTool.createTool(), ListVirtualBookFramesTool.createHandler(context))
						.build();
					
					logger.info("MCP server built successfully (session factory should now be set)");
					
					// Start transport provider AFTER building the server (so session factory is set)
					logger.info("Starting transport provider...");
					transportProvider.start();
					logger.info("Transport provider started");
					
					logger.info("=== MCP Server Thread Completed Successfully ===");
				} catch (IOException e) {
					logger.error("MCP Server thread IOException", e);
					logger.error("IOException details: " + e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					logger.error("MCP Server thread error", e);
					logger.error("Exception type: " + e.getClass().getName());
					logger.error("Exception message: " + e.getMessage());
					if (e.getCause() != null) {
						logger.error("Caused by: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
					}
					e.printStackTrace();
				}
			}, "MCP-Server-Thread-SDK");
			
			logger.info("Server thread created: " + serverThread.getName());
			serverThread.setDaemon(true);
			logger.info("Starting server thread...");
			serverThread.start();
			logger.info("Server thread started, waiting 1 second for initialization...");
			
			// Wait a bit to see if server starts
			Thread.sleep(1000);
			
			if (transportProvider != null) {
				logger.info("SUCCESS: MCP HTTP Server is RUNNING on port " + httpPort);
			} else {
				logger.warn("WARNING: MCP HTTP Server thread started but transport provider is NULL");
			}
			
			logger.info("MCP Server started in background thread (HTTP mode with SDK)");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Interrupted while starting MCP Server", e);
		} catch (Exception e) {
			logger.error("Error starting MCP Server Manager", e);
			logger.error("Exception type: " + e.getClass().getName());
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
		}
		logger.info("=== MCPServerManager.start() completed ===");
	}
	
	/**
	 * Stop the MCP server
	 */
	public void stop() {
		logger.info("Stopping MCP Server (SDK-based)...");
		
		if (transportProvider != null) {
			logger.info("Stopping transport provider...");
			transportProvider.stop();
			transportProvider = null;
		}
		
		if (mcpServer != null) {
			logger.info("Closing MCP server...");
			mcpServer.close();
			mcpServer = null;
		}
		
		if (serverThread != null && serverThread.isAlive()) {
			try {
				serverThread.join(2000); // Wait up to 2 seconds
			} catch (InterruptedException e) {
				logger.warn("Interrupted while waiting for server thread to stop", e);
				Thread.currentThread().interrupt();
			}
			serverThread = null;
		}
		
		logger.info("MCP Server stopped");
	}
	
	/**
	 * Check if the server is running
	 */
	public boolean isRunning() {
		return transportProvider != null;
	}
	
	/**
	 * Get the HTTP port if using HTTP mode
	 */
	public int getHttpPort() {
		return transportProvider != null ? httpPort : -1;
	}
}
