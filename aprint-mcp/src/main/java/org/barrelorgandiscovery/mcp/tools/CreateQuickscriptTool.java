package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.QuickScriptManager;
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
 * Tool for creating a new quickscript and opening it in a visual editor.
 * 
 * @author APrint Development Team
 */
public class CreateQuickscriptTool {
	
	private static final Logger logger = Logger.getLogger(CreateQuickscriptTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> nameProperty = new HashMap<>();
		nameProperty.put("type", "string");
		nameProperty.put("description", "Name of the new quickscript to create");
		
		Map<String, Object> initialContentProperty = new HashMap<>();
		initialContentProperty.put("type", "string");
		initialContentProperty.put("description", "Initial script content (optional)");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", nameProperty);
		properties.put("initialContent", initialContentProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("name"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("create_quickscript")
			.description("Create a new quickscript and open it in a visual editor console. Returns a resource URI that can be used to interact with the console.")
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
				if (args == null || !args.containsKey("name")) {
					throw new IllegalArgumentException("Missing required parameter: name");
				}
				
				String scriptName = args.get("name").toString();
				String initialContent = args.containsKey("initialContent") ? 
					args.get("initialContent").toString() : "";
				
				logger.info("Creating quickscript: " + scriptName);
				
				QuickScriptManager manager = context.getQuickScriptManager();
				if (manager == null) {
					throw new IllegalStateException("QuickScriptManager is not available");
				}
				
				// Create empty script file
				manager.saveScript(scriptName, new StringBuffer(initialContent));
				
				// Open in editor
				String title = "New Script: " + scriptName;
				String resourceUri = context.openScriptConsole(scriptName, null, title, false);
				
				Map<String, Object> response = new HashMap<>();
				response.put("resourceUri", resourceUri);
				response.put("scriptName", scriptName);
				response.put("message", "Quickscript created and console opened successfully");
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error creating quickscript: " + e.getMessage(), e);
				
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

