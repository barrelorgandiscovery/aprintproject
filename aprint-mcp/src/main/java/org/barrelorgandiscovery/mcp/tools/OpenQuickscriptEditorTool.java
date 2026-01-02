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
 * Tool for opening a quickscript in a visual editor console.
 * 
 * @author APrint Development Team
 */
public class OpenQuickscriptEditorTool {
	
	private static final Logger logger = Logger.getLogger(OpenQuickscriptEditorTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> nameProperty = new HashMap<>();
		nameProperty.put("type", "string");
		nameProperty.put("description", "Name of the quickscript to open in the editor");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", nameProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("name"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("open_quickscript_editor")
			.description("Open a quickscript in a visual editor console. Returns a resource URI that can be used to interact with the console.")
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
				logger.info("Opening quickscript editor: " + scriptName);
				
				String title = "Script: " + scriptName;
				String resourceUri = context.openScriptConsole(scriptName, null, title, false);
				
				Map<String, Object> response = new HashMap<>();
				response.put("resourceUri", resourceUri);
				response.put("scriptName", scriptName);
				response.put("message", "Console opened successfully");
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error opening quickscript editor: " + e.getMessage(), e);
				
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
