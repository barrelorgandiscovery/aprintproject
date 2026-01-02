package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
import org.barrelorgandiscovery.mcp.ComponentSearchCriteria;
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
 * Tool for finding components matching criteria.
 * 
 * @author APrint Development Team
 */
public class FindComponentsTool {
	
	private static final Logger logger = Logger.getLogger(FindComponentsTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> windowIdProperty = new HashMap<>();
		windowIdProperty.put("type", "string");
		windowIdProperty.put("description", "ID of the window (frameId or console resource URI). If null, uses active window.");
		
		Map<String, Object> nameProperty = new HashMap<>();
		nameProperty.put("type", "string");
		nameProperty.put("description", "Filter by component name (getName())");
		
		Map<String, Object> typeProperty = new HashMap<>();
		typeProperty.put("type", "string");
		typeProperty.put("description", "Filter by component type (e.g., 'JButton', 'JTextField')");
		
		Map<String, Object> actionCommandProperty = new HashMap<>();
		actionCommandProperty.put("type", "string");
		actionCommandProperty.put("description", "Filter by action command (for buttons/menus)");
		
		Map<String, Object> textProperty = new HashMap<>();
		textProperty.put("type", "string");
		textProperty.put("description", "Filter by text content (partial match)");
		
		Map<String, Object> criteriaProperties = new HashMap<>();
		criteriaProperties.put("name", nameProperty);
		criteriaProperties.put("type", typeProperty);
		criteriaProperties.put("actionCommand", actionCommandProperty);
		criteriaProperties.put("text", textProperty);
		
		Map<String, Object> criteriaProperty = new HashMap<>();
		criteriaProperty.put("type", "object");
		criteriaProperty.put("properties", criteriaProperties);
		criteriaProperty.put("description", "Search criteria (all fields are optional)");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("windowId", windowIdProperty);
		properties.put("criteria", criteriaProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of(), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("find_components")
			.description("Find Swing components matching search criteria. Can search by name, type, action command, or text content.")
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
				
				ComponentSearchCriteria criteria = new ComponentSearchCriteria();
				if (args != null && args.containsKey("criteria")) {
					@SuppressWarnings("unchecked")
					Map<String, Object> criteriaMap = (Map<String, Object>) args.get("criteria");
					if (criteriaMap != null) {
						if (criteriaMap.containsKey("name")) {
							criteria.setName(criteriaMap.get("name").toString());
						}
						if (criteriaMap.containsKey("type")) {
							criteria.setType(criteriaMap.get("type").toString());
						}
						if (criteriaMap.containsKey("actionCommand")) {
							criteria.setActionCommand(criteriaMap.get("actionCommand").toString());
						}
						if (criteriaMap.containsKey("text")) {
							criteria.setText(criteriaMap.get("text").toString());
						}
					}
				}
				
				logger.info("Finding components: windowId=" + windowId + ", criteria=" + criteria);
				
				List<SwingComponentInfo> components = context.findComponents(windowId, criteria);
				
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
				logger.error("Error finding components: " + e.getMessage(), e);
				
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
