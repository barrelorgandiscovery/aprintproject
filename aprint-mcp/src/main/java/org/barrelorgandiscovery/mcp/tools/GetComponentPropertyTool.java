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
 * Tool for getting a specific property of a Swing component.
 * 
 * @author APrint Development Team
 */
public class GetComponentPropertyTool {
	
	private static final Logger logger = Logger.getLogger(GetComponentPropertyTool.class);
	
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
		
		Map<String, Object> propertyNameProperty = new HashMap<>();
		propertyNameProperty.put("type", "string");
		propertyNameProperty.put("description", "Name of the property (e.g., 'text', 'enabled', 'visible', 'toolTipText')");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("windowId", windowIdProperty);
		properties.put("componentPath", componentPathProperty);
		properties.put("propertyName", propertyNameProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("componentPath", "propertyName"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("get_component_property")
			.description("Get a specific property of a Swing component. Common properties: text, name, visible, enabled, toolTipText, etc.")
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
				if (args == null || !args.containsKey("componentPath") || !args.containsKey("propertyName")) {
					throw new IllegalArgumentException("Missing required parameters: componentPath and propertyName");
				}
				
				String windowId = args.containsKey("windowId") ? args.get("windowId").toString() : null;
				String componentPath = args.get("componentPath").toString();
				String propertyName = args.get("propertyName").toString();
				
				logger.info("Getting component property: windowId=" + windowId + ", componentPath=" + componentPath + ", propertyName=" + propertyName);
				
				Object value = context.getComponentProperty(windowId, componentPath, propertyName);
				
				Map<String, Object> response = new HashMap<>();
				response.put("componentPath", componentPath);
				response.put("propertyName", propertyName);
				response.put("value", value);
				response.put("valueType", value != null ? value.getClass().getName() : null);
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error getting component property: " + e.getMessage(), e);
				
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
