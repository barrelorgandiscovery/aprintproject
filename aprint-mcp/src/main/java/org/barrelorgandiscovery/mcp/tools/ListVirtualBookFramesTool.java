package org.barrelorgandiscovery.mcp.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.aprintng.APrintNGInternalFrame;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import java.io.IOException;

/**
 * Tool for listing all open virtual book frames.
 * 
 * @author APrint Development Team
 */
public class ListVirtualBookFramesTool {
	
	private static final Logger logger = Logger.getLogger(ListVirtualBookFramesTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		JsonSchema inputSchema = new JsonSchema("object", Collections.emptyMap(), 
			null, null, null, null);
		
		return McpSchema.Tool.builder()
			.name("list_virtual_book_frames")
			.description("List all currently open virtual book frames with their information.")
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
				// Get the application instance
				APrintNGGeneralServices app = context.getApplication();
				
				if (!(app instanceof APrintNG)) {
					Map<String, Object> response = new HashMap<>();
					response.put("error", "Application instance not available");
					String resultText = jsonMapper.writeValueAsString(response);
					List<Content> content = List.of(new TextContent(resultText));
					return CallToolResult.builder()
						.content(content)
						.isError(false)
						.build();
				}
				
				APrintNG aprintNG = (APrintNG) app;
				// Use listVirtualBookFrames() to get frames with their IDs
				Map<String, APrintNGVirtualBookFrame> framesMap = aprintNG.listVirtualBookFrames();
				
				List<Map<String, Object>> frameList = new ArrayList<>();
				
				for (Map.Entry<String, APrintNGVirtualBookFrame> entry : framesMap.entrySet()) {
					String frameId = entry.getKey();
					APrintNGVirtualBookFrame vbFrame = entry.getValue();
					
					if (vbFrame != null) {
						Map<String, Object> frameInfo = new HashMap<>();
						
						// Include the frame ID for MCP operations
						frameInfo.put("frameId", frameId);
						frameInfo.put("title", vbFrame instanceof APrintNGInternalFrame 
							? ((APrintNGInternalFrame) vbFrame).getTitle() 
							: "Unknown");
						frameInfo.put("hasVirtualBook", vbFrame.getVirtualBook() != null);
						
						if (vbFrame.getVirtualBook() != null) {
							VirtualBook virtualBook = vbFrame.getVirtualBook();
							Scale associatedScale = virtualBook.getScale();
							frameInfo.put("holeCount", virtualBook.getHolesCopy().size());
							frameInfo.put("trackCount", associatedScale.getTrackNb());
							frameInfo.put("scale", associatedScale.getName());
						}
						
						frameInfo.put("instrument", vbFrame.getCurrentInstrument() != null 
							? vbFrame.getCurrentInstrument().getName() 
							: "null");
						
						frameList.add(frameInfo);
					}
				}
				
				Map<String, Object> response = new HashMap<>();
				response.put("frames", frameList);
				response.put("count", frameList.size());
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error executing tool: " + e.getMessage(), e);
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
				} catch (IOException ioException) {
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
