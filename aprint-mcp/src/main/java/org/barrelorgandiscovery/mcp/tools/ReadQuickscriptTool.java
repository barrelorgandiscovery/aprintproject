package org.barrelorgandiscovery.mcp.tools;

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
 * Tool for reading a quickscript content.
 * 
 * @author APrint Development Team
 */
public class ReadQuickscriptTool {
	
	private static final Logger logger = Logger.getLogger(ReadQuickscriptTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> nameProperty = new HashMap<>();
		nameProperty.put("type", "string");
		nameProperty.put("description", "Name of the quickscript to read");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", nameProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("name"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("read_quickscript")
			.description("Read the content of a quickscript by name")
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
				Map<String, Object> args = request.arguments();
				if (args == null || !args.containsKey("name")) {
					throw new IllegalArgumentException("Missing required parameter: name");
				}
				
				String scriptName = args.get("name").toString();
				logger.info("Reading quickscript: " + scriptName);
				
				QuickScriptManager manager = context.getQuickScriptManager();
				if (manager == null) {
					throw new IllegalStateException("QuickScriptManager is not available");
				}
				
				StringBuffer scriptContent = manager.loadScript(scriptName);
				
				Map<String, Object> response = new HashMap<>();
				response.put("name", scriptName);
				response.put("content", scriptContent.toString());
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error reading quickscript: " + e.getMessage(), e);
				
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

