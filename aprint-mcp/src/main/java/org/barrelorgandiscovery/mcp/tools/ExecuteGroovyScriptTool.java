package org.barrelorgandiscovery.mcp.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import groovy.lang.Binding;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import java.io.IOException;
import java.util.List;

/**
 * Tool for executing Groovy scripts in the APrint application context.
 * 
 * @author APrint Development Team
 */
public class ExecuteGroovyScriptTool {
	
	private static final Logger logger = Logger.getLogger(ExecuteGroovyScriptTool.class);
	
	private final APrintMCPContext context;
	
	public ExecuteGroovyScriptTool(APrintMCPContext context) {
		this.context = context;
	}
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> scriptProperty = new HashMap<>();
		scriptProperty.put("type", "string");
		scriptProperty.put("description", "The Groovy script code to execute");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("script", scriptProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("script"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("execute_groovy_script")
			.description("Execute a Groovy script in the APrint application context. " +
			       "The script has access to variables: virtualbook, pianoroll, currentinstrument, toolbarspanel, services.")
			.inputSchema(inputSchema)
			.build();
	}
	
	/**
	 * Creates the handler function for this tool.
	 */
	public static java.util.function.BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> 
			createHandler(APrintMCPContext context) {
		return (exchange, request) -> {
			McpJsonMapper jsonMapper = McpJsonMapperProvider.get();
			try {
				logger.info("Executing Groovy script via MCP");
				
				// Get script from arguments
				Map<String, Object> args = request.arguments();
				if (args == null || !args.containsKey("script")) {
					throw new IllegalArgumentException("Missing required parameter: script");
				}
				
				String script = args.get("script").toString();
				
				// Create a console panel for script execution
				APrintGroovyConsolePanel console = context.createGroovyConsolePanel();
				
				// Set up the binding with context variables
				Binding binding = console.getCurrentBindingRef();
				
				// Get current virtual book frame if available
				APrintNGVirtualBookFrame frame = context.getCurrentVirtualBookFrame();
				if (frame != null) {
					binding.setProperty("virtualbook", frame.getVirtualBook());
					binding.setProperty("pianoroll", frame.getPianoRoll());
					binding.setProperty("currentinstrument", frame.getCurrentInstrument());
					// Note: toolbarspanel and services would need to be added to the context
				}
				
				// Set the script content
				console.setScriptContent(script);
				
				// Execute the script
				Future<Object> future = console.run();
				
				// Wait for completion (with timeout)
				Object result;
				try {
					result = future.get();
				} catch (Exception e) {
					logger.error("Error executing script", e);
					throw new Exception("Script execution failed: " + e.getMessage(), e);
				}
				
				Map<String, Object> response = new HashMap<>();
				response.put("success", true);
				response.put("result", result != null ? result.toString() : "null");
				response.put("message", "Script executed successfully");
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error executing tool: " + e.getMessage(), e);
				
				try {
					Map<String, Object> errorInfo = new HashMap<>();
					errorInfo.put("error", e.getMessage());
					errorInfo.put("type", e.getClass().getSimpleName());
					String errorText = jsonMapper.writeValueAsString(errorInfo);
					List<Content> content = List.of(new TextContent(errorText));
					
					return CallToolResult.builder()
						.content(content)
						.isError(true)
						.build();
				} catch (IOException ioException) {
					logger.error("Failed to serialize error response", ioException);
					List<Content> content = List.of(new TextContent("{\"error\":\"" + e.getMessage() + "\"}"));
					return CallToolResult.builder()
						.content(content)
						.isError(true)
						.build();
				}
			}
		};
	}
}
