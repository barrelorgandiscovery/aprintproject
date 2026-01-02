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
 * Tool for getting the value of a Swing component.
 * 
 * @author APrint Development Team
 */
public class GetComponentValueTool {
	
	private static final Logger logger = Logger.getLogger(GetComponentValueTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> windowIdProperty = new HashMap<>();
		windowIdProperty.put("type", "string");
		windowIdProperty.put("description", "ID of the window (frameId or console resource URI). If null, uses active window.");
		
		Map<String, Object> componentPathProperty = new HashMap<>();
		componentPathProperty.put("type", "string");
		componentPathProperty.put("description", "Path to the component (e.g., 'frame_1/toolbarPanel/button_0')");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("windowId", windowIdProperty);
		properties.put("componentPath", componentPathProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("componentPath"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("get_component_value")
			.description("Get the value of a Swing component. Returns text for text components, selection state for checkboxes, selected item for combos, etc.")
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
				if (args == null || !args.containsKey("componentPath")) {
					throw new IllegalArgumentException("Missing required parameter: componentPath");
				}
				
				String windowId = args.containsKey("windowId") ? args.get("windowId").toString() : null;
				String componentPath = args.get("componentPath").toString();
				
				logger.info("Getting component value: windowId=" + windowId + ", componentPath=" + componentPath);
				
				Object value = context.getComponentValue(windowId, componentPath);
				
				Map<String, Object> response = new HashMap<>();
				response.put("componentPath", componentPath);
				response.put("value", value);
				response.put("valueType", value != null ? value.getClass().getName() : null);
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error getting component value: " + e.getMessage(), e);
				
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
