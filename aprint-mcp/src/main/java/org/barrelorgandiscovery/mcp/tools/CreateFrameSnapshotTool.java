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
 * Tool for creating a snapshot (screenshot) of a frame/window and providing it to the AI.
 * Returns the image as a base64-encoded PNG string.
 * 
 * @author APrint Development Team
 */
public class CreateFrameSnapshotTool {
	
	private static final Logger logger = Logger.getLogger(CreateFrameSnapshotTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> windowIdProperty = new HashMap<>();
		windowIdProperty.put("type", "string");
		windowIdProperty.put("description", "ID of the window (frameId or console resource URI). If null, uses active window.");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("windowId", windowIdProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of(), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("create_frame_snapshot")
			.description("Create a snapshot (screenshot) of a frame/window and return it as a base64-encoded PNG image. " +
			       "The snapshot can be used by the AI to see the current state of the GUI. " +
			       "If windowId is not provided, captures the currently active window.")
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
				
				logger.info("Creating frame snapshot: windowId=" + windowId);
				
				String base64Image = context.createFrameSnapshot(windowId);
				
				if (base64Image == null) {
					Map<String, Object> response = new HashMap<>();
					response.put("success", false);
					response.put("error", "Failed to capture snapshot. Window may not be visible or accessible.");
					response.put("windowId", windowId);
					
					String resultText = jsonMapper.writeValueAsString(response);
					List<Content> content = List.of(new TextContent(resultText));
					
					return CallToolResult.builder()
						.content(content)
						.isError(false)
						.build();
				}
				
				Map<String, Object> response = new HashMap<>();
				response.put("success", true);
				response.put("windowId", windowId);
				response.put("imageBase64", base64Image);
				response.put("format", "PNG");
				response.put("message", "Snapshot created successfully");
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error creating frame snapshot: " + e.getMessage(), e);
				
				try {
					Map<String, Object> errorInfo = new HashMap<>();
					errorInfo.put("success", false);
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
					List<Content> content = List.of(new TextContent("{\"success\":false,\"error\":\"" + e.getMessage() + "\"}"));
					return CallToolResult.builder()
						.content(content)
						.isError(true)
						.build();
				}
			}
		};
	}
	
	

}
