package org.barrelorgandiscovery.mcp.resources;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookInternalFrame;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.ActiveWindowInfo;
import org.barrelorgandiscovery.mcp.InstrumentInfo;
import org.barrelorgandiscovery.mcp.McpBundledDocumentation;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
import org.barrelorgandiscovery.mcp.LibrarySearchResult;
import org.barrelorgandiscovery.mcp.ScaleInfo;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

/**
 * MCP resources and URI templates for {@code aprint://} — instruments, scales, open books,
 * GUI/visual context, Lucene library search (same index as the Search panel), and Groovy scripting docs.
 */
public final class AprintMcpResourceRegistry {

	private static final Logger logger = Logger.getLogger(AprintMcpResourceRegistry.class);

	public static final String URI_MANIFEST = "aprint://context/manifest.json";
	public static final String URI_INDEX_MD = "aprint://context/index.md";
	public static final String URI_CATALOG_INSTRUMENTS = "aprint://catalog/instruments.json";
	public static final String URI_CATALOG_SCALES = "aprint://catalog/scales.json";
	public static final String URI_VIRTUALBOOK_CURRENT = "aprint://virtualbook/current.json";
	public static final String URI_GUI_WINDOWS = "aprint://gui/windows.json";
	public static final String URI_GUI_ACTIVE = "aprint://gui/active.json";
	public static final String URI_GUI_VISUAL = "aprint://gui/visual-summary.json";
	/** Bundled Markdown: Groovy scripting guide (same as {@link McpBundledDocumentation#URI_GROOVY_SCRIPTING}). */
	public static final String URI_DOCS_GROOVY_SCRIPTING = McpBundledDocumentation.URI_GROOVY_SCRIPTING;

	private AprintMcpResourceRegistry() {
	}

	public static McpServerFeatures.SyncResourceSpecification[] staticResources(APrintMCPContext ctx) {
		return new McpServerFeatures.SyncResourceSpecification[] {
			spec(URI_MANIFEST, "manifest", "APrint MCP manifest",
				"JSON index of static URIs and URI templates for this server.",
				"application/json", (ex, req) -> readManifest(ctx, req)),
			spec(URI_INDEX_MD, "index", "APrint MCP resource index",
				"Human-readable list of aprint:// resources for assistants.",
				"text/markdown", (ex, req) -> readIndexMarkdown(req)),
			spec(URI_CATALOG_INSTRUMENTS, "instruments_catalog", "Instrument names (repository)",
				"JSON: all instrument names from the current repository.",
				"application/json", (ex, req) -> readInstrumentsCatalog(ctx, req)),
			spec(URI_CATALOG_SCALES, "scales_catalog", "Scale names (repository)",
				"JSON: all scale (gamme) names from the repository.",
				"application/json", (ex, req) -> readScalesCatalog(ctx, req)),
			spec(URI_VIRTUALBOOK_CURRENT, "virtualbook_current", "Current virtual book",
				"JSON summary of the active VirtualBook frame (holes, scale, instrument).",
				"application/json", (ex, req) -> readVirtualBookCurrent(ctx, req)),
			spec(URI_GUI_WINDOWS, "gui_windows", "Open windows",
				"JSON: all open windows (ids, titles, types) from MCP window tracking.",
				"application/json", (ex, req) -> readGuiWindows(ctx, req)),
			spec(URI_GUI_ACTIVE, "gui_active", "Active window",
				"JSON: currently active window (VirtualBook or script console).",
				"application/json", (ex, req) -> readGuiActive(ctx, req)),
			spec(URI_GUI_VISUAL, "gui_visual_summary", "GUI visual summary",
				"JSON: active window plus compact list of open books and instruments.",
				"application/json", (ex, req) -> readGuiVisualSummary(ctx, req)),
			spec(URI_DOCS_GROOVY_SCRIPTING, "docs_groovy_scripting", "Groovy scripting guide",
				"Markdown: APrint Studio Groovy API — variables, repository (gammes/instruments), carton, helpers.",
				"text/markdown", (ex, req) -> readGroovyScriptingDocumentation(req)),
		};
	}

	public static McpServerFeatures.SyncResourceTemplateSpecification[] resourceTemplates(APrintMCPContext ctx) {
		return new McpServerFeatures.SyncResourceTemplateSpecification[] {
			tplSpec(
				ResourceTemplate.builder()
					.uriTemplate("aprint://instrument/{name}")
					.name("instrument_detail")
					.title("Instrument (repository)")
					.description("Full instrument metadata. Use URL-encoded {name} (path segment).")
					.mimeType("application/json")
					.build(),
				(ex, req) -> readInstrumentByName(ctx, req)),
			tplSpec(
				ResourceTemplate.builder()
					.uriTemplate("aprint://scale/{name}")
					.name("scale_detail")
					.title("Scale / gamme (repository)")
					.description("Full scale metadata. Use URL-encoded {name}.")
					.mimeType("application/json")
					.build(),
				(ex, req) -> readScaleByName(ctx, req)),
			tplSpec(
				ResourceTemplate.builder()
					.uriTemplate("aprint://virtualbook/frame/{frameId}")
					.name("virtualbook_frame")
					.title("Virtual book by frame id")
					.description("JSON summary for a specific open VirtualBook frame (MCP frame id).")
					.mimeType("application/json")
					.build(),
				(ex, req) -> readVirtualBookFrame(ctx, req)),
			tplSpec(
				ResourceTemplate.builder()
					.uriTemplate("aprint://library/search/{query}")
					.name("library_search")
					.title("Indexed book search")
					.description("Lucene search over indexed .book library (same as Search panel). "
						+ "{query} is one URI-encoded path segment (use %20 for spaces).")
					.mimeType("application/json")
					.build(),
				(ex, req) -> readLibrarySearch(ctx, req)),
		};
	}

	private static McpServerFeatures.SyncResourceSpecification spec(String uri, String name, String title,
			String description, String mimeType,
			McpReadHandler handler) {
		Resource resource = Resource.builder()
			.uri(uri)
			.name(name)
			.title(title)
			.description(description)
			.mimeType(mimeType)
			.build();
		return new McpServerFeatures.SyncResourceSpecification(resource,
			(exchange, request) -> handler.read(exchange, request));
	}

	private static McpServerFeatures.SyncResourceTemplateSpecification tplSpec(ResourceTemplate template,
			McpReadHandler handler) {
		return new McpServerFeatures.SyncResourceTemplateSpecification(template,
			(exchange, request) -> handler.read(exchange, request));
	}

	@FunctionalInterface
	private interface McpReadHandler {
		ReadResourceResult read(McpSyncServerExchange exchange, ReadResourceRequest request);
	}

	private static ReadResourceResult jsonResult(String uri, Map<String, Object> map) {
		return textJson(uri, toJson(map));
	}

	private static ReadResourceResult textJson(String uri, String json) {
		return new ReadResourceResult(List.of(new TextResourceContents(uri, "application/json", json)));
	}

	private static ReadResourceResult textMarkdown(String uri, String md) {
		return new ReadResourceResult(List.of(new TextResourceContents(uri, "text/markdown", md)));
	}

	private static String toJson(Object o) {
		try {
			McpJsonMapper m = McpJsonMapperProvider.get();
			return m.writeValueAsString(o);
		} catch (IOException e) {
			logger.error("JSON serialize", e);
			return "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}";
		}
	}

	private static String escapeJson(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static ReadResourceResult readManifest(APrintMCPContext ctx, ReadResourceRequest req) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("scheme", "aprint");
		m.put("staticResources", List.of(
			Map.of("uri", URI_MANIFEST, "mimeType", "application/json"),
			Map.of("uri", URI_INDEX_MD, "mimeType", "text/markdown"),
			Map.of("uri", URI_CATALOG_INSTRUMENTS, "mimeType", "application/json"),
			Map.of("uri", URI_CATALOG_SCALES, "mimeType", "application/json"),
			Map.of("uri", URI_VIRTUALBOOK_CURRENT, "mimeType", "application/json"),
			Map.of("uri", URI_GUI_WINDOWS, "mimeType", "application/json"),
			Map.of("uri", URI_GUI_ACTIVE, "mimeType", "application/json"),
			Map.of("uri", URI_GUI_VISUAL, "mimeType", "application/json"),
			Map.of("uri", URI_DOCS_GROOVY_SCRIPTING, "mimeType", "text/markdown")));
		m.put("resourceTemplates", List.of(
			Map.of("uriTemplate", "aprint://instrument/{name}", "mimeType", "application/json"),
			Map.of("uriTemplate", "aprint://scale/{name}", "mimeType", "application/json"),
			Map.of("uriTemplate", "aprint://virtualbook/frame/{frameId}", "mimeType", "application/json"),
			Map.of("uriTemplate", "aprint://library/search/{query}", "mimeType", "application/json")));
		m.put("notes", "Use tools (list_components, create_frame_snapshot) for deep Swing inspection.");
		return jsonResult(req.uri(), m);
	}

	private static ReadResourceResult readIndexMarkdown(ReadResourceRequest req) {
		String md = "# APrint MCP resources (`aprint://`)\n\n"
			+ "## Static\n"
			+ "- `" + URI_MANIFEST + "` — machine-readable manifest\n"
			+ "- `" + URI_CATALOG_INSTRUMENTS + "` — instrument name list\n"
			+ "- `" + URI_CATALOG_SCALES + "` — scale name list\n"
			+ "- `" + URI_VIRTUALBOOK_CURRENT + "` — active book summary\n"
			+ "- `" + URI_GUI_WINDOWS + "` / `" + URI_GUI_ACTIVE + "` / `" + URI_GUI_VISUAL + "` — GUI context\n"
			+ "- `" + URI_DOCS_GROOVY_SCRIPTING + "` — **Groovy scripting** (gammes, instruments, carton)\n\n"
			+ "## Templates\n"
			+ "- `aprint://instrument/{name}` — instrument details (URL-encode `name`)\n"
			+ "- `aprint://scale/{name}` — scale details\n"
			+ "- `aprint://virtualbook/frame/{frameId}` — open book by MCP frame id\n"
			+ "- `aprint://library/search/{query}` — Lucene search (encode query as one segment)\n";
		return textMarkdown(req.uri(), md);
	}

	private static ReadResourceResult readInstrumentsCatalog(APrintMCPContext ctx, ReadResourceRequest req) {
		String[] names = ctx.listInstruments();
		Map<String, Object> m = new HashMap<>();
		m.put("count", names.length);
		m.put("names", List.of(names));
		return jsonResult(req.uri(), m);
	}

	private static ReadResourceResult readScalesCatalog(APrintMCPContext ctx, ReadResourceRequest req) {
		String[] names = ctx.listScales();
		Map<String, Object> m = new HashMap<>();
		m.put("count", names.length);
		m.put("names", List.of(names));
		return jsonResult(req.uri(), m);
	}

	private static ReadResourceResult readVirtualBookCurrent(APrintMCPContext ctx, ReadResourceRequest req) {
		APrintNGVirtualBookFrame frame = ctx.getCurrentVirtualBookFrame();
		Map<String, Object> m = new LinkedHashMap<>();
		if (frame == null) {
			m.put("error", "No active VirtualBook frame");
			return jsonResult(req.uri(), m);
		}
		fillFrameSummary(ctx, frame, m);
		return jsonResult(req.uri(), m);
	}

	private static void fillFrameSummary(APrintMCPContext ctx, APrintNGVirtualBookFrame frame, Map<String, Object> m) {
		String frameId = null;
		for (Map.Entry<String, APrintNGVirtualBookFrame> e : ctx.listVirtualBookFrames().entrySet()) {
			if (e.getValue() == frame) {
				frameId = e.getKey();
				break;
			}
		}
		m.put("frameId", frameId);
		if (frame instanceof APrintNGVirtualBookInternalFrame) {
			m.put("frameTitle", ((APrintNGVirtualBookInternalFrame) frame).getTitle());
		}
		VirtualBook vb = frame.getVirtualBook();
		m.put("hasVirtualBook", vb != null);
		if (vb != null) {
			m.put("bookName", vb.getName());
			m.put("holeCount", vb.getHolesCopy().size());
			m.put("lengthMicros", vb.getLength());
			if (vb.getScale() != null) {
				m.put("scaleLabel", vb.getScale().toString());
				m.put("trackCount", vb.getScale().getTrackNb());
			}
		}
		if (frame.getCurrentInstrument() != null) {
			m.put("instrument", frame.getCurrentInstrument().getName());
		}
	}

	private static ReadResourceResult readGuiWindows(APrintMCPContext ctx, ReadResourceRequest req) {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (ActiveWindowInfo w : ctx.listAllWindows()) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("windowId", w.getWindowId());
			row.put("title", w.getTitle());
			row.put("type", w.getType() != null ? w.getType().name() : null);
			row.put("frameId", w.getFrameId());
			row.put("resourceUri", w.getResourceUri());
			rows.add(row);
		}
		return jsonResult(req.uri(), Map.of("windows", rows, "count", rows.size()));
	}

	private static ReadResourceResult readGuiActive(APrintMCPContext ctx, ReadResourceRequest req) {
		ActiveWindowInfo a = ctx.getActiveWindow();
		if (a == null) {
			return jsonResult(req.uri(), Map.of("active", Map.of()));
		}
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("windowId", a.getWindowId());
		m.put("title", a.getTitle());
		m.put("type", a.getType() != null ? a.getType().name() : null);
		m.put("frameId", a.getFrameId());
		m.put("resourceUri", a.getResourceUri());
		return jsonResult(req.uri(), Map.of("active", m));
	}

	private static ReadResourceResult readGuiVisualSummary(APrintMCPContext ctx, ReadResourceRequest req) {
		Map<String, Object> root = new LinkedHashMap<>();
		ActiveWindowInfo a = ctx.getActiveWindow();
		if (a != null) {
			Map<String, Object> act = new LinkedHashMap<>();
			act.put("windowId", a.getWindowId());
			act.put("title", a.getTitle());
			act.put("type", a.getType() != null ? a.getType().name() : null);
			root.put("activeWindow", act);
		} else {
			root.put("activeWindow", null);
		}
		List<Map<String, Object>> books = new ArrayList<>();
		for (Map.Entry<String, APrintNGVirtualBookFrame> e : ctx.listVirtualBookFrames().entrySet()) {
			APrintNGVirtualBookFrame f = e.getValue();
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("frameId", e.getKey());
			if (f instanceof APrintNGVirtualBookInternalFrame) {
				row.put("title", ((APrintNGVirtualBookInternalFrame) f).getTitle());
			}
			if (f.getVirtualBook() != null) {
				row.put("bookName", f.getVirtualBook().getName());
			}
			if (f.getCurrentInstrument() != null) {
				row.put("instrument", f.getCurrentInstrument().getName());
			}
			books.add(row);
		}
		root.put("openVirtualBooks", books);
		if (ctx.getApplication() instanceof APrintNG) {
			root.put("aprintVersion", ((APrintNG) ctx.getApplication()).getVersion());
		}
		return jsonResult(req.uri(), root);
	}

	private static ReadResourceResult readInstrumentByName(APrintMCPContext ctx, ReadResourceRequest req) {
		String name = extractTemplateParam(req.uri(), "aprint://instrument/");
		Map<String, Object> m = new LinkedHashMap<>();
		if (name == null || name.isEmpty()) {
			m.put("error", "missing instrument name");
			return jsonResult(req.uri(), m);
		}
		InstrumentInfo info = ctx.getInstrumentInfo(name);
		if (info == null) {
			m.put("error", "instrument not found");
			m.put("name", name);
			return jsonResult(req.uri(), m);
		}
		m.putAll(info.toMap());
		return jsonResult(req.uri(), m);
	}

	private static ReadResourceResult readScaleByName(APrintMCPContext ctx, ReadResourceRequest req) {
		String name = extractTemplateParam(req.uri(), "aprint://scale/");
		Map<String, Object> m = new LinkedHashMap<>();
		if (name == null || name.isEmpty()) {
			m.put("error", "missing scale name");
			return jsonResult(req.uri(), m);
		}
		ScaleInfo info = ctx.getScaleInfo(name);
		if (info == null) {
			m.put("error", "scale not found");
			m.put("name", name);
			return jsonResult(req.uri(), m);
		}
		m.putAll(info.toMap());
		return jsonResult(req.uri(), m);
	}

	private static ReadResourceResult readVirtualBookFrame(APrintMCPContext ctx, ReadResourceRequest req) {
		String uri = req.uri();
		String prefix = "aprint://virtualbook/frame/";
		if (!uri.startsWith(prefix)) {
			return jsonResult(uri, Map.of("error", "invalid virtual book resource uri"));
		}
		String frameId = uri.substring(prefix.length());
		try {
			frameId = URLDecoder.decode(frameId, StandardCharsets.UTF_8);
		} catch (Exception ignored) {
		}
		APrintNGVirtualBookFrame frame = ctx.getVirtualBookFrame(frameId);
		Map<String, Object> m = new LinkedHashMap<>();
		if (frame == null) {
			m.put("error", "frame not found or closed");
			m.put("frameId", frameId);
			return jsonResult(req.uri(), m);
		}
		m.put("frameId", frameId);
		fillFrameSummary(ctx, frame, m);
		return jsonResult(req.uri(), m);
	}

	private static ReadResourceResult readGroovyScriptingDocumentation(ReadResourceRequest req) {
		try {
			String md = McpBundledDocumentation.loadClasspathUtf8(McpBundledDocumentation.CLASSPATH_GROOVY_SCRIPTING);
			return textMarkdown(req.uri(), md);
		} catch (IOException e) {
			logger.error("load Groovy scripting documentation", e);
			String fallback = "# Documentation Groovy\n\nContenu indisponible : " + e.getMessage() + "\n";
			return textMarkdown(req.uri(), fallback);
		}
	}

	private static ReadResourceResult readLibrarySearch(APrintMCPContext ctx, ReadResourceRequest req) {
		String uri = req.uri();
		String prefix = "aprint://library/search/";
		if (!uri.startsWith(prefix)) {
			return jsonResult(uri, Map.of("error", "invalid library search uri"));
		}
		String q = uri.substring(prefix.length());
		try {
			q = URLDecoder.decode(q, StandardCharsets.UTF_8);
		} catch (Exception ignored) {
		}
		LibrarySearchResult search = ctx.searchIndexedBooks(q, 100);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("query", q);
		body.putAll(search.toMap());
		return jsonResult(req.uri(), body);
	}

	/**
	 * Path after prefix for a single-segment template param (instrument / scale / search query).
	 */
	private static String extractTemplateParam(String uri, String prefix) {
		if (uri == null || !uri.startsWith(prefix)) {
			return null;
		}
		String rest = uri.substring(prefix.length());
		try {
			return URLDecoder.decode(rest, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return rest;
		}
	}
}
