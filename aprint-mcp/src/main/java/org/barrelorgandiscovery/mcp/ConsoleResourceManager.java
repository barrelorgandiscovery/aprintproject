package org.barrelorgandiscovery.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.swing.JDialog;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Manages console resources for MCP.
 * Tracks open script consoles and provides access via resource URIs.
 * 
 * @author APrint Development Team
 */
public class ConsoleResourceManager {
	
	private static final Logger logger = Logger.getLogger(ConsoleResourceManager.class);
	
	private final Map<String, ConsoleResource> openConsoles = new ConcurrentHashMap<>();
	
	/**
	 * Register a new console and return its resource URI
	 */
	public String registerConsole(String scriptName, String windowId, 
	                               APrintGroovyConsolePanel console, JDialog dialog) {
		String resourceUri = "aprint://console/" + windowId;
		ConsoleResource resource = new ConsoleResource(scriptName, windowId, console, dialog);
		openConsoles.put(resourceUri, resource);
		logger.debug("Registered console: " + resourceUri);
		return resourceUri;
	}
	
	/**
	 * Unregister a console
	 */
	public void unregisterConsole(String resourceUri) {
		openConsoles.remove(resourceUri);
		logger.debug("Unregistered console: " + resourceUri);
	}
	
	/**
	 * List all console resources for MCP
	 */
	public List<McpSchema.Resource> listResources() {
		// Clean up closed consoles
		List<String> toRemove = new ArrayList<>();
		for (Map.Entry<String, ConsoleResource> entry : openConsoles.entrySet()) {
			if (!entry.getValue().isOpen()) {
				toRemove.add(entry.getKey());
			}
		}
		for (String uri : toRemove) {
			openConsoles.remove(uri);
		}
		
		return openConsoles.entrySet().stream()
			.map(entry -> McpSchema.Resource.builder()
				.uri(entry.getKey())
				.name("Console: " + entry.getValue().getScriptName())
				.description("Script console for '" + entry.getValue().getScriptName() + "'")
				.mimeType("application/json")
				.build())
			.collect(Collectors.toList());
	}
	
	/**
	 * Read a console resource
	 */
	public String readResource(String uri) {
		ConsoleResource resource = openConsoles.get(uri);
		if (resource == null) {
			throw new IllegalArgumentException("Resource not found: " + uri);
		}
		return resource.toJson();
	}
	
	/**
	 * Check if a console is open
	 */
	public boolean isConsoleOpen(String resourceUri) {
		ConsoleResource resource = openConsoles.get(resourceUri);
		return resource != null && resource.isOpen();
	}
	
	/**
	 * Set the script content in a console
	 */
	public void setConsoleScript(String resourceUri, String scriptContent) {
		ConsoleResource resource = openConsoles.get(resourceUri);
		if (resource == null) {
			throw new IllegalArgumentException("Console not found: " + resourceUri);
		}
		if (resource.isReadonly()) {
			throw new IllegalStateException("Console is readonly: " + resourceUri);
		}
		resource.getConsole().setScriptContent(scriptContent);
		resource.markAsModified();
		logger.debug("Updated script in console: " + resourceUri);
	}
	
	/**
	 * Execute the script in a console
	 */
	public Future<Object> executeConsoleScript(String resourceUri) {
		ConsoleResource resource = openConsoles.get(resourceUri);
		if (resource == null) {
			throw new IllegalArgumentException("Console not found: " + resourceUri);
		}
		long startTime = System.currentTimeMillis();
		Future<Object> future = resource.getConsole().run();
		
		// Note: We can't easily track the result here since Future is async
		// The result tracking would need to be done via a callback or polling
		
		return future;
	}
	
	/**
	 * Get the console panel for a resource URI
	 */
	public APrintGroovyConsolePanel getConsole(String resourceUri) {
		ConsoleResource resource = openConsoles.get(resourceUri);
		if (resource == null) {
			throw new IllegalArgumentException("Console not found: " + resourceUri);
		}
		return resource.getConsole();
	}
	
	/**
	 * Get the console resource for a resource URI (for internal use)
	 */
	public ConsoleResource getConsoleResource(String resourceUri) {
		return openConsoles.get(resourceUri);
	}
	
	/**
	 * Get all open consoles (for internal use)
	 */
	public Map<String, ConsoleResource> getAllConsoles() {
		return openConsoles;
	}
}

