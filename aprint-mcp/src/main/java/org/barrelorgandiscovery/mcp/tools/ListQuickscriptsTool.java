package org.barrelorgandiscovery.mcp.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.QuickScriptManager;
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

/**
 * Tool for listing available quickscripts.
 * 
 * @author APrint Development Team
 */
public class ListQuickscriptsTool {
	
	private static final Logger logger = Logger.getLogger(ListQuickscriptsTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		JsonSchema inputSchema = new JsonSchema("object", new HashMap<>(), 
			List.of(), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("list_quickscripts")
			.description("List all available quickscripts in the APrint application")
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
				logger.info("Listing quickscripts via MCP");
				
				QuickScriptManager manager = context.getQuickScriptManager();
				if (manager == null) {
					throw new IllegalStateException("QuickScriptManager is not available");
				}
				
				String[] scripts = manager.listQuickScripts();
				List<Map<String, Object>> scriptList = new ArrayList<>();
				
				for (String scriptName : scripts) {
					Map<String, Object> scriptInfo = new HashMap<>();
					scriptInfo.put("name", scriptName);
					scriptList.add(scriptInfo);
				}
				
				Map<String, Object> response = new HashMap<>();
				response.put("scripts", scriptList);
				response.put("count", scriptList.size());
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error listing quickscripts: " + e.getMessage(), e);
				
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
	
	private static McpJsonMapper getJsonMapper() {
		return ServiceLoader.load(McpJsonMapperSupplier.class)
			.stream()
			.findFirst()
			.map(ServiceLoader.Provider::get)
			.map(McpJsonMapperSupplier::get)
			.orElseThrow(() -> new IllegalStateException("No McpJsonMapperSupplier found on classpath"));
	}
}

