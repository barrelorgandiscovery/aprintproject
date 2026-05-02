package org.barrelorgandiscovery.mcp.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpBundledDocumentation;
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
 * Exposes the same Markdown as {@link org.barrelorgandiscovery.mcp.resources.AprintMcpResourceRegistry}
 * static resource {@value org.barrelorgandiscovery.mcp.McpBundledDocumentation#URI_GROOVY_SCRIPTING} for clients that use tools only.
 */
public final class GetGroovyScriptingDocumentationTool {

	private static final Logger logger = Logger.getLogger(GetGroovyScriptingDocumentationTool.class);

	private GetGroovyScriptingDocumentationTool() {
	}

	public static McpSchema.Tool createTool() {
		JsonSchema inputSchema = new JsonSchema("object", Collections.emptyMap(),
			null, null, null, null);
		return McpSchema.Tool.builder()
			.name("get_groovy_scripting_documentation")
			.description(
				"Returns bundled Markdown documentation for APrint Studio Groovy scripting: predefined variables "
					+ "(virtualbook, pianoroll, currentinstrument, services), repository saveScale/saveInstrument, "
					+ "groovy.aprint.transform helpers (ScaleHelper, TransformHelper), virtual book elements, "
					+ "and official doc URLs. Same content as MCP resource "
					+ McpBundledDocumentation.URI_GROOVY_SCRIPTING + ".")
			.inputSchema(inputSchema)
			.build();
	}

	public static java.util.function.BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> createHandler(
			@SuppressWarnings("unused") APrintMCPContext context) {
		return (exchange, request) -> {
			McpJsonMapper jsonMapper = McpJsonMapperProvider.get();
			try {
				String md = McpBundledDocumentation.loadClasspathUtf8(McpBundledDocumentation.CLASSPATH_GROOVY_SCRIPTING);
				Map<String, Object> payload = new HashMap<>();
				payload.put("resourceUri", McpBundledDocumentation.URI_GROOVY_SCRIPTING);
				payload.put("mimeType", "text/markdown");
				payload.put("markdown", md);
				String json = jsonMapper.writeValueAsString(payload);
				List<Content> content = List.of(new TextContent(json));
				return CallToolResult.builder().content(content).isError(false).build();
			} catch (Exception e) {
				logger.error("get_groovy_scripting_documentation", e);
				try {
					Map<String, Object> err = new HashMap<>();
					err.put("error", e.getMessage());
					err.put("type", e.getClass().getSimpleName());
					String errorText = jsonMapper.writeValueAsString(err);
					return CallToolResult.builder()
						.content(List.of(new TextContent(errorText)))
						.isError(true)
						.build();
				} catch (Exception e2) {
					return CallToolResult.builder()
						.content(List.of(new TextContent("{\"error\":\"failed to load documentation\"}")))
						.isError(true)
						.build();
				}
			}
		};
	}
}
