package org.barrelorgandiscovery.mcp.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.mcp.APrintMCPContext;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.McpJsonMapperSupplier;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import java.io.IOException;
import java.util.ServiceLoader;

/**
 * Tool for stopping playback of the current virtual book.
 * 
 * @author APrint Development Team
 */
public class TriggerStopTool {
	
	private static final Logger logger = Logger.getLogger(TriggerStopTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		JsonSchema inputSchema = new JsonSchema("object", Collections.emptyMap(), 
			null, null, null, null);
		
		return McpSchema.Tool.builder()
			.name("trigger_stop")
			.description("Stop playing the currently active virtual book.")
			.inputSchema(inputSchema)
			.build();
	}
	
	/**
	 * Creates the handler function for this tool.
	 */
	public static java.util.function.BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> 
			createHandler(APrintMCPContext context) {
		return (exchange, request) -> {
			McpJsonMapper jsonMapper = getJsonMapper();
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
				
				frame.stop();
				Map<String, Object> response = new HashMap<>();
				response.put("success", true);
				response.put("message", "Playback stopped");
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error stopping playback: " + e.getMessage(), e);
				try {
					Map<String, Object> response = new HashMap<>();
					response.put("success", false);
					response.put("error", e.getMessage());
					String resultText = jsonMapper.writeValueAsString(response);
					List<Content> content = List.of(new TextContent(resultText));
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
	
	private static McpJsonMapper getJsonMapper() {
		return ServiceLoader.load(McpJsonMapperSupplier.class)
			.stream()
			.findFirst()
			.map(ServiceLoader.Provider::get)
			.map(McpJsonMapperSupplier::get)
			.orElseThrow(() -> new IllegalStateException("No McpJsonMapperSupplier found on classpath"));
	}
}
