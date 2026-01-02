package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
import org.barrelorgandiscovery.mcp.SwingComponentInfo;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tool for listing all components in a window.
 * 
 * @author APrint Development Team
 */
public class ListComponentsTool {
	
	private static final Logger logger = Logger.getLogger(ListComponentsTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> windowIdProperty = new HashMap<>();
		windowIdProperty.put("type", "string");
		windowIdProperty.put("description", "ID of the window (frameId or console resource URI). If null, uses active window.");
		
		Map<String, Object> filterTypeProperty = new HashMap<>();
		filterTypeProperty.put("type", "string");
		filterTypeProperty.put("description", "Optional filter by component type (e.g., 'JButton', 'JTextField')");
		
		Map<String, Object> maxDepthProperty = new HashMap<>();
		maxDepthProperty.put("type", "integer");
		maxDepthProperty.put("description", "Maximum depth to traverse (default: 10)");
		maxDepthProperty.put("default", 10);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("windowId", windowIdProperty);
		properties.put("filterType", filterTypeProperty);
		properties.put("maxDepth", maxDepthProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of(), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("list_components")
			.description("List all Swing components in a window. Returns component information including type, name, text, visibility, and properties.")
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
				String windowId = args != null && args.containsKey("windowId") ? 
					args.get("windowId").toString() : null;
				String filterType = args != null && args.containsKey("filterType") ? 
					args.get("filterType").toString() : null;
				int maxDepth = args != null && args.containsKey("maxDepth") ? 
					((Number) args.get("maxDepth")).intValue() : 10;
				
				logger.info("Listing components: windowId=" + windowId + ", filterType=" + filterType + ", maxDepth=" + maxDepth);
				
				List<SwingComponentInfo> components = context.listComponents(windowId, filterType, maxDepth);
				
				Map<String, Object> response = new HashMap<>();
				response.put("components", components.stream().map(SwingComponentInfo::toMap).toList());
				response.put("count", components.size());
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error listing components: " + e.getMessage(), e);
				
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
