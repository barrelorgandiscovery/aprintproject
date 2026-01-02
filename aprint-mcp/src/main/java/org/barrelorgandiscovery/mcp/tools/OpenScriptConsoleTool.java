package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tool for opening a temporary script console with initial content.
 * 
 * @author APrint Development Team
 */
public class OpenScriptConsoleTool {
	
	private static final Logger logger = Logger.getLogger(OpenScriptConsoleTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> scriptContentProperty = new HashMap<>();
		scriptContentProperty.put("type", "string");
		scriptContentProperty.put("description", "Initial script content for the console");
		
		Map<String, Object> titleProperty = new HashMap<>();
		titleProperty.put("type", "string");
		titleProperty.put("description", "Window title (optional)");
		
		Map<String, Object> readonlyProperty = new HashMap<>();
		readonlyProperty.put("type", "boolean");
		readonlyProperty.put("description", "If true, script is read-only (optional, default: false)");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("scriptContent", scriptContentProperty);
		properties.put("title", titleProperty);
		properties.put("readonly", readonlyProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("scriptContent"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("open_script_console")
			.description("Open a temporary script console with initial content. Returns a resource URI that can be used to interact with the console.")
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
				Map<String, Object> args = request.arguments();
				if (args == null || !args.containsKey("scriptContent")) {
					throw new IllegalArgumentException("Missing required parameter: scriptContent");
				}
				
				String scriptContent = args.get("scriptContent").toString();
				String title = args.containsKey("title") ? args.get("title").toString() : "Script Console";
				boolean readonly = args.containsKey("readonly") && Boolean.TRUE.equals(args.get("readonly"));
				
				logger.info("Opening script console");
				
				String resourceUri = context.openScriptConsole(null, scriptContent, title, readonly);
				
				Map<String, Object> response = new HashMap<>();
				response.put("resourceUri", resourceUri);
				response.put("message", "Console opened successfully");
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error opening script console: " + e.getMessage(), e);
				
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
	
	

}
