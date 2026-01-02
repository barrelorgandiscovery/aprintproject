package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
import org.barrelorgandiscovery.mcp.InstrumentInfo;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tool for getting detailed information about an instrument.
 * 
 * @author APrint Development Team
 */
public class GetInstrumentInfoTool {
	
	private static final Logger logger = Logger.getLogger(GetInstrumentInfoTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> instrumentNameProperty = new HashMap<>();
		instrumentNameProperty.put("type", "string");
		instrumentNameProperty.put("description", "Name of the instrument to get information about");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("instrumentName", instrumentNameProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("instrumentName"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("get_instrument_info")
			.description("Get detailed information about an instrument by name. Returns instrument details including scale name, description URL, track count, etc.")
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
				if (args == null || !args.containsKey("instrumentName")) {
					throw new IllegalArgumentException("Missing required parameter: instrumentName");
				}
				
				String instrumentName = args.get("instrumentName").toString();
				
				logger.info("Getting instrument info: " + instrumentName);
				
				InstrumentInfo info = context.getInstrumentInfo(instrumentName);
				
				if (info == null) {
					Map<String, Object> response = new HashMap<>();
					response.put("found", false);
					response.put("message", "Instrument not found: " + instrumentName);
					
					String resultText = jsonMapper.writeValueAsString(response);
					List<Content> content = List.of(new TextContent(resultText));
					
					return CallToolResult.builder()
						.content(content)
						.isError(false)
						.build();
				}
				
				String resultText = jsonMapper.writeValueAsString(info.toMap());
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error getting instrument info: " + e.getMessage(), e);
				
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
