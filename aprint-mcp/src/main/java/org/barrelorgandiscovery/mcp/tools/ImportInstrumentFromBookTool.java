package org.barrelorgandiscovery.mcp.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.ImportInstrumentFromBookRequest;
import org.barrelorgandiscovery.mcp.ImportInstrumentFromBookResult;
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
 * MCP tool: import gamme depuis un .book + création d'instrument (SF2 cloné).
 * Voir {@link ImportInstrumentFromBookRequest} (dryRun, garde-fous) et {@link ImportInstrumentFromBookResult} (compatibility).
 */
public class ImportInstrumentFromBookTool {

	private static final Logger logger = Logger.getLogger(ImportInstrumentFromBookTool.class);

	private static boolean argBoolean(Map<String, Object> args, String key, boolean defaultValue) {
		if (args == null || !args.containsKey(key) || args.get(key) == null) {
			return defaultValue;
		}
		Object v = args.get(key);
		if (v instanceof Boolean) {
			return (Boolean) v;
		}
		return Boolean.parseBoolean(v.toString());
	}

	public static McpSchema.Tool createTool() {
		Map<String, Object> pathProp = new HashMap<>();
		pathProp.put("type", "string");
		pathProp.put("description",
			"Absolute path to a .book that embeds the gamme (e.g. .../00 Gamme 52 Fournier Bay.book).");

		Map<String, Object> srcProp = new HashMap<>();
		srcProp.put("type", "string");
		srcProp.put("description",
			"Repository instrument to copy soundbank + registerpatch from (required).");

		Map<String, Object> nameProp = new HashMap<>();
		nameProp.put("type", "string");
		nameProp.put("description",
			"Name for the new instrument. If omitted, uses the book metadata DesignedInstrumentName when present.");

		Map<String, Object> dryProp = new HashMap<>();
		dryProp.put("type", "boolean");
		dryProp.put("description",
			"If true, only runs compatibility analysis (compatibility in result); no write to repository. Use first to avoid duplicate instruments.");

		Map<String, Object> owProp = new HashMap<>();
		owProp.put("type", "boolean");
		owProp.put("description",
			"If true and the target instrument name already exists, delete it then import. Default false.");

		Map<String, Object> abortProp = new HashMap<>();
		abortProp.put("type", "boolean");
		abortProp.put("description",
			"If true, refuse import when the repository already has an instrument with the same scale definition. Default false.");

		Map<String, Object> properties = new HashMap<>();
		properties.put("referenceBookPath", pathProp);
		properties.put("sourceInstrumentName", srcProp);
		properties.put("newInstrumentName", nameProp);
		properties.put("dryRun", dryProp);
		properties.put("allowOverwrite", owProp);
		properties.put("abortIfCompatibleInstrumentExists", abortProp);

		JsonSchema inputSchema = new JsonSchema("object", properties,
			List.of("referenceBookPath", "sourceInstrumentName"), null, null, null);

		return McpSchema.Tool.builder()
			.name("import_instrument_from_book")
			.description(
				"Register the scale from a .book and save a cloned instrument (SF2 + register mappings). "
					+ "Use dryRun=true first: result includes compatibility (existing instruments with same scale, name collision, source vs book scale). "
					+ "Set abortIfCompatibleInstrumentExists=true to block when an equivalent instrument already exists. "
					+ "allowOverwrite replaces an existing instrument with the same name. "
					+ "Returns importPerformed and compatibility in JSON.")
			.inputSchema(inputSchema)
			.build();
	}

	public static java.util.function.BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult>
			createHandler(APrintMCPContext context) {
		return (exchange, request) -> {
			McpJsonMapper jsonMapper = McpJsonMapperProvider.get();
			try {
				Map<String, Object> args = request.arguments();
				if (args == null || !args.containsKey("referenceBookPath")
					|| args.get("referenceBookPath") == null) {
					throw new IllegalArgumentException("Missing required parameter: referenceBookPath");
				}
				if (!args.containsKey("sourceInstrumentName") || args.get("sourceInstrumentName") == null
					|| args.get("sourceInstrumentName").toString().trim().isEmpty()) {
					throw new IllegalArgumentException("Missing required parameter: sourceInstrumentName");
				}
				String path = args.get("referenceBookPath").toString();
				String src = args.get("sourceInstrumentName").toString();
				String newName = args != null && args.containsKey("newInstrumentName") && args.get("newInstrumentName") != null
					? args.get("newInstrumentName").toString() : null;
				boolean dryRun = argBoolean(args, "dryRun", false);
				boolean allowOverwrite = argBoolean(args, "allowOverwrite", false);
				boolean abortCompat = argBoolean(args, "abortIfCompatibleInstrumentExists", false);

				ImportInstrumentFromBookRequest req = new ImportInstrumentFromBookRequest(
					path, src, newName, dryRun, allowOverwrite, abortCompat);
				logger.info("import_instrument_from_book " + req.toMap());

				ImportInstrumentFromBookResult result = context.importInstrumentFromBook(req);
				String resultText = jsonMapper.writeValueAsString(result.toMap());
				return CallToolResult.builder()
					.content(List.of(new TextContent(resultText)))
					.isError(!result.isSuccess())
					.build();
			} catch (Exception e) {
				logger.error("import_instrument_from_book", e);
				try {
					ImportInstrumentFromBookResult err = ImportInstrumentFromBookResult.failure(e, null, null, null, null);
					return CallToolResult.builder()
						.content(List.of(new TextContent(jsonMapper.writeValueAsString(err.toMap()))))
						.isError(true)
						.build();
				} catch (Exception e2) {
					return CallToolResult.builder()
						.content(List.of(new TextContent("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}")))
						.isError(true)
						.build();
				}
			}
		};
	}
}
