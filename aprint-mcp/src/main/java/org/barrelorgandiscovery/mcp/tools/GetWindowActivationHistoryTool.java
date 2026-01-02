package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
import org.barrelorgandiscovery.mcp.WindowActivationEvent;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tool for getting the window activation history.
 * Allows MCP to see which windows the user has been working with.
 * 
 * @author APrint Development Team
 */
public class GetWindowActivationHistoryTool {
	
	private static final Logger logger = Logger.getLogger(GetWindowActivationHistoryTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> limitProperty = new HashMap<>();
		limitProperty.put("type", "integer");
		limitProperty.put("description", "Maximum number of events to return (0 for all, default: 20)");
		limitProperty.put("default", 20);
		
		Map<String, Object> windowIdProperty = new HashMap<>();
		windowIdProperty.put("type", "string");
		windowIdProperty.put("description", "Optional: filter by specific window ID. If provided, returns only history for that window.");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("limit", limitProperty);
		properties.put("windowId", windowIdProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of(), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("get_window_activation_history")
			.description("Get the history of window activations. Shows which windows the user has been working with, " +
			       "ordered by most recent activation. Useful for understanding user workflow and providing context-aware assistance.")
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
				int limit = args != null && args.containsKey("limit") ? 
					((Number) args.get("limit")).intValue() : 20;
				String windowId = args != null && args.containsKey("windowId") ? 
					args.get("windowId").toString() : null;
				
				logger.info("Getting window activation history: limit=" + limit + ", windowId=" + windowId);
				
				java.util.List<WindowActivationEvent> history;
				if (windowId != null && !windowId.isEmpty()) {
					history = context.getWindowActivationHistoryForWindow(windowId);
				} else {
					history = context.getWindowActivationHistory(limit);
				}
				
				Map<String, Object> response = new HashMap<>();
				response.put("events", history.stream().map(WindowActivationEvent::toMap).toList());
				response.put("count", history.size());
				
				// Add current active window info
				WindowActivationEvent current = context.getCurrentActiveWindowFromHistory();
				if (current != null) {
					response.put("currentActiveWindow", current.toMap());
				}
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error getting window activation history: " + e.getMessage(), e);
				
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
