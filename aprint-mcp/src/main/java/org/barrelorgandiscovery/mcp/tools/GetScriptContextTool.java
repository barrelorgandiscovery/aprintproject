package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
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
 * Tool for getting the script context (available variables) for a frame.
 * 
 * @author APrint Development Team
 */
public class GetScriptContextTool {
	
	private static final Logger logger = Logger.getLogger(GetScriptContextTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> frameIdProperty = new HashMap<>();
		frameIdProperty.put("type", "string");
		frameIdProperty.put("description", "Frame ID (optional, uses current frame if not provided)");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("frameId", frameIdProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of(), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("get_script_context")
			.description("Get the script context (available variables) for a VirtualBookFrame. Returns information about available objects and their types.")
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
				String frameId = args != null && args.containsKey("frameId") ? 
					args.get("frameId").toString() : null;
				
				logger.info("Getting script context for frame: " + (frameId != null ? frameId : "current"));
				
				APrintNGVirtualBookFrame frame = frameId != null ? 
					context.getVirtualBookFrame(frameId) : context.getCurrentVirtualBookFrame();
				
				if (frame == null) {
					throw new IllegalArgumentException("No VirtualBookFrame available");
				}
				
				Map<String, Object> contextInfo = new HashMap<>();
				contextInfo.put("frameId", frameId);
				contextInfo.put("hasVirtualBook", frame.getVirtualBook() != null);
				contextInfo.put("hasPianoRoll", frame.getPianoRoll() != null);
				contextInfo.put("hasCurrentInstrument", frame.getCurrentInstrument() != null);
				
				// Add type information
				Map<String, String> availableVariables = new HashMap<>();
				if (frame.getVirtualBook() != null) {
					availableVariables.put("virtualbook", frame.getVirtualBook().getClass().getName());
				}
				if (frame.getPianoRoll() != null) {
					availableVariables.put("pianoroll", frame.getPianoRoll().getClass().getName());
				}
				if (frame.getCurrentInstrument() != null) {
					availableVariables.put("currentinstrument", frame.getCurrentInstrument().getClass().getName());
				}
				availableVariables.put("services", context.getApplication().getClass().getName());
				
				contextInfo.put("availableVariables", availableVariables);
				
				String resultText = jsonMapper.writeValueAsString(contextInfo);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error getting script context: " + e.getMessage(), e);
				
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
