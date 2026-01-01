package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.McpJsonMapperSupplier;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tool for executing the script in an open console.
 * 
 * @author APrint Development Team
 */
public class ExecuteConsoleScriptTool {
	
	private static final Logger logger = Logger.getLogger(ExecuteConsoleScriptTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> resourceUriProperty = new HashMap<>();
		resourceUriProperty.put("type", "string");
		resourceUriProperty.put("description", "Resource URI of the console (from open_quickscript_editor, create_quickscript, or open_script_console)");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("resourceUri", resourceUriProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("resourceUri"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("execute_console_script")
			.description("Execute the script currently in an open console. The execution happens asynchronously and results are displayed in the console.")
			.inputSchema(inputSchema)
			.build();
	}
	
	/**
	 * Creates the handler function for this tool.
	 */
	public static java.util.function.BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> 
			createHandler(APrintMCPContext context) {
		return (exchange, request) -> {
			McpJsonMapper jsonMapper = getJsonMapper();
			try {
				Map<String, Object> args = request.arguments();
				if (args == null || !args.containsKey("resourceUri")) {
					throw new IllegalArgumentException("Missing required parameter: resourceUri");
				}
				
				String resourceUri = args.get("resourceUri").toString();
				
				logger.info("Executing console script: " + resourceUri);
				
				Future<Object> future = context.getConsoleResourceManager().executeConsoleScript(resourceUri);
				
				Map<String, Object> response = new HashMap<>();
				response.put("resourceUri", resourceUri);
				response.put("message", "Script execution started. Results will appear in the console.");
				response.put("async", true);
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error executing console script: " + e.getMessage(), e);
				
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
				} catch (Exception ioException) {
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
	
	private static McpJsonMapper getJsonMapper() {
		return ServiceLoader.load(McpJsonMapperSupplier.class)
			.stream()
			.findFirst()
			.map(ServiceLoader.Provider::get)
			.map(McpJsonMapperSupplier::get)
			.orElseThrow(() -> new IllegalStateException("No McpJsonMapperSupplier found on classpath"));
	}
}

