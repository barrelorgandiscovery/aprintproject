package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.LibrarySearchResult;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
import org.barrelorgandiscovery.mcp.SearchLibraryRequest;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * MCP tool wrapping {@link APrintMCPContext#searchIndexedBooks(String, int)} (Lucene index, same as Search panel).
 */
public class SearchIndexedBooksTool {

	private static final Logger logger = Logger.getLogger(SearchIndexedBooksTool.class);

	public static McpSchema.Tool createTool() {
		Map<String, Object> queryProp = new HashMap<>();
		queryProp.put("type", "string");
		queryProp.put("description",
			"Lucene query (same syntax as APrint Search panel). Use plain text or field queries, e.g. scale:\"52 Limonaire\" or description:fournier. Empty string returns many hits (up to maxResults).");

		Map<String, Object> maxProp = new HashMap<>();
		maxProp.put("type", "integer");
		maxProp.put("description", "Maximum number of hits (default 50, capped at 200).");

		Map<String, Object> properties = new HashMap<>();
		properties.put("query", queryProp);
		properties.put("maxResults", maxProp);

		JsonSchema inputSchema = new JsonSchema("object", properties,
			List.of("query"), null, null, null);

		return McpSchema.Tool.builder()
			.name("search_library")
			.description(
				"Search the indexed .book library (Lucene, same index as the Search panel). "
					+ "Returns typed LibrarySearchResult JSON: success, hits (IndexedBookHit[]), count, or error.")
			.inputSchema(inputSchema)
			.build();
	}

	public static java.util.function.BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult>
			createHandler(APrintMCPContext context) {
		return (exchange, request) -> {
			McpJsonMapper jsonMapper = McpJsonMapperProvider.get();
			try {
				SearchLibraryRequest sl = SearchLibraryRequest.fromToolArguments(request.arguments());
				logger.info("search_library query=\"" + sl.getQuery() + "\" maxResults=" + sl.getMaxResults());

				LibrarySearchResult result = context.searchIndexedBooks(sl.getQuery(), sl.getMaxResults());
				String resultText = jsonMapper.writeValueAsString(result.toMap());
				return CallToolResult.builder()
					.content(List.of(new TextContent(resultText)))
					.isError(!result.isSuccess())
					.build();

			} catch (Exception e) {
				logger.error("search_library: " + e.getMessage(), e);
				try {
					LibrarySearchResult err = LibrarySearchResult.failure(
						e.getMessage() != null ? e.getMessage() : e.toString());
					String errorText = jsonMapper.writeValueAsString(err.toMap());
					return CallToolResult.builder()
						.content(List.of(new TextContent(errorText)))
						.isError(true)
						.build();
				} catch (Exception ioException) {
					List<Content> content = List.of(new TextContent("{\"success\":false,\"error\":\"" + e.getMessage() + "\"}"));
					return CallToolResult.builder()
						.content(content)
						.isError(true)
						.build();
				}
			}
		};
	}
}
