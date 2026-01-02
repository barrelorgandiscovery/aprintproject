package org.barrelorgandiscovery.mcp.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
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
 * Tool for getting information about the current virtual book.
 * 
 * @author APrint Development Team
 */
public class GetVirtualBookInfoTool {
	
	private static final Logger logger = Logger.getLogger(GetVirtualBookInfoTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		JsonSchema inputSchema = new JsonSchema("object", Collections.emptyMap(), 
			null, null, null, null);
		
		return McpSchema.Tool.builder()
			.name("get_virtual_book_info")
			.description("Get information about the currently active virtual book, including scale, number of holes, tracks, etc.")
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
				APrintNGVirtualBookFrame frame = context.getCurrentVirtualBookFrame();
				
				if (frame == null) {
					Map<String, Object> response = new HashMap<>();
					response.put("error", "No virtual book frame is currently active");
					String resultText = jsonMapper.writeValueAsString(response);
					List<Content> content = List.of(new TextContent(resultText));
					return CallToolResult.builder()
						.content(content)
						.isError(false)
						.build();
				}
				
				VirtualBook virtualBook = frame.getVirtualBook();
				
				Map<String, Object> info = new HashMap<>();
				info.put("hasVirtualBook", virtualBook != null);
				
				if (virtualBook != null) {
					info.put("holeCount", virtualBook.getHolesCopy().size());
					info.put("scale", virtualBook.getScale() != null ? virtualBook.getScale().toString() : "null");
					if (virtualBook.getScale() != null) {
						info.put("trackCount", virtualBook.getScale().getTrackNb());
					}
					info.put("length", virtualBook.getLength());
				}
				
				info.put("instrument", frame.getCurrentInstrument() != null 
					? frame.getCurrentInstrument().getName() 
					: "null");
				
				String resultText = jsonMapper.writeValueAsString(info);
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
