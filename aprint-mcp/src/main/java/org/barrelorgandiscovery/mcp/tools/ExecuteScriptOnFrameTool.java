package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
import org.barrelorgandiscovery.mcp.ScriptExecutionResult;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tool for executing a script directly on a VirtualBookFrame.
 * This is for direct execution by the AI to explore objects and get information.
 * 
 * @author APrint Development Team
 */
public class ExecuteScriptOnFrameTool {
	
	private static final Logger logger = Logger.getLogger(ExecuteScriptOnFrameTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		Map<String, Object> frameIdProperty = new HashMap<>();
		frameIdProperty.put("type", "string");
		frameIdProperty.put("description", "Frame ID (optional, uses current frame if not provided)");
		
		Map<String, Object> scriptProperty = new HashMap<>();
		scriptProperty.put("type", "string");
		scriptProperty.put("description", "Groovy script code to execute");
		
		Map<String, Object> captureOutputProperty = new HashMap<>();
		captureOutputProperty.put("type", "boolean");
		captureOutputProperty.put("description", "If true, capture stdout/stderr (optional, default: true)");
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("frameId", frameIdProperty);
		properties.put("script", scriptProperty);
		properties.put("captureOutput", captureOutputProperty);
		
		JsonSchema inputSchema = new JsonSchema("object", properties, 
			List.of("script"), null, null, null);
		
		return McpSchema.Tool.builder()
			.name("execute_script_on_frame")
			.description("Execute a Groovy script directly on a VirtualBookFrame. " +
			       "The script has access to variables: virtualbook, pianoroll, currentinstrument, services. " +
			       "This is for direct execution by the AI to explore objects and get information. " +
			       "Returns the execution result with captured output.")
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
				if (args == null || !args.containsKey("script")) {
					throw new IllegalArgumentException("Missing required parameter: script");
				}
				
				String frameId = args.containsKey("frameId") ? args.get("frameId").toString() : null;
				String script = args.get("script").toString();
				boolean captureOutput = !args.containsKey("captureOutput") || Boolean.TRUE.equals(args.get("captureOutput"));
				
				logger.info("Executing script on frame: " + (frameId != null ? frameId : "current"));
				
				ScriptExecutionResult result = context.executeScriptOnFrame(frameId, script, captureOutput);
				
				String resultText = jsonMapper.writeValueAsString(result.toMap());
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(!result.isSuccess())
					.build();
					
			} catch (Exception e) {
				logger.error("Error executing script on frame: " + e.getMessage(), e);
				
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
