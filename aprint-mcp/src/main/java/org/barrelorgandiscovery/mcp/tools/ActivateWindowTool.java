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
 * Tool for activating/focusing a window by its ID.
 * 
 * @author APrint Development Team
 */
public class ActivateWindowTool {
	
	private static final Logger logger = Logger.getLogger(ActivateWindowTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> windowIdProperty = new HashMap<>();
		windowIdProperty.put("type", "string");
		windowIdProperty.put("description", "ID of the window to activate (frameId, console resource URI, or 'main' for main window)");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("windowId", windowIdProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("windowId"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("activate_window")
			.description("Activate/focus a window by its ID. Brings the window to the front and gives it focus. " +
			       "Useful for switching between multiple open windows.")
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
				if (args == null || !args.containsKey("windowId")) {
					throw new IllegalArgumentException("Missing required parameter: windowId");
				}
				
				String windowId = args.get("windowId").toString();
				
				logger.info("Activating window: " + windowId);
				
				boolean success = context.activateWindow(windowId);
				
				Map<String, Object> response = new HashMap<>();
				response.put("windowId", windowId);
				response.put("activated", success);
				if (!success) {
					response.put("message", "Window not found or could not be activated: " + windowId);
				}
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error activating window: " + e.getMessage(), e);
				
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
