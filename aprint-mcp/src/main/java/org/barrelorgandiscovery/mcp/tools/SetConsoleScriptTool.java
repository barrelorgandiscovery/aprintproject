package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;
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
 * Tool for setting/updating the script content in an open console.
 * 
 * @author APrint Development Team
 */
public class SetConsoleScriptTool {
	
	private static final Logger logger = Logger.getLogger(SetConsoleScriptTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> resourceUriProperty = new HashMap<>();
		resourceUriProperty.put("type", "string");
		resourceUriProperty.put("description", "Resource URI of the console (from open_quickscript_editor, create_quickscript, or open_script_console)");
		
		Map<String, Object> scriptContentProperty = new HashMap<>();
		scriptContentProperty.put("type", "string");
		scriptContentProperty.put("description", "New script content to set in the console");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("resourceUri", resourceUriProperty);
		properties.put("scriptContent", scriptContentProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("resourceUri", "scriptContent"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("set_console_script")
			.description("Set or update the script content in an open console. The console must be opened and not readonly.")
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
				if (args == null || !args.containsKey("resourceUri") || !args.containsKey("scriptContent")) {
					throw new IllegalArgumentException("Missing required parameters: resourceUri, scriptContent");
				}
				
				String resourceUri = args.get("resourceUri").toString();
				String scriptContent = args.get("scriptContent").toString();
				
				logger.info("Setting console script: " + resourceUri);
				
				context.getConsoleResourceManager().setConsoleScript(resourceUri, scriptContent);
				
				Map<String, Object> response = new HashMap<>();
				response.put("resourceUri", resourceUri);
				response.put("message", "Script content updated successfully");
				
				String resultText = jsonMapper.writeValueAsString(response);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error setting console script: " + e.getMessage(), e);
				
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

