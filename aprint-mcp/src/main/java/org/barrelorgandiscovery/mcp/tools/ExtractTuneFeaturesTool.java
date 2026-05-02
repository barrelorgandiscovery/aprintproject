package org.barrelorgandiscovery.mcp.tools;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.mcp.APrintMCPContext;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;
import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureExtractorManager;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tool for extracting musical features directly from the currently opened virtual book.
 * This tool analyzes the tune using native feature extractors and provides
 * recommendations for transformations and enhancements.
 * 
 * @author APrint Development Team
 */
public class ExtractTuneFeaturesTool {
	
	private static final Logger logger = Logger.getLogger(ExtractTuneFeaturesTool.class);
	
	/**
	 * Creates the SDK Tool definition.
	 */
	public static McpSchema.Tool createTool() {
		JsonSchema inputSchema = new JsonSchema("object", Collections.emptyMap(), 
			null, null, null, null);
		
		return McpSchema.Tool.builder()
			.name("extract_tune_features")
			.description("Extract musical features directly from the currently opened virtual book. " +
				"Returns statistical features covering pitch, rhythm, harmony, and texture. " +
				"Use search_library to find a .book by scale or keywords, open it in APrint, then call this tool on the active frame.")
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
				
				VirtualBook virtualBook = frame.getVirtualBook();
				if (virtualBook == null) {
					Map<String, Object> response = new HashMap<>();
					response.put("error", "No virtual book is loaded in the current frame");
					String resultText = jsonMapper.writeValueAsString(response);
					List<Content> content = List.of(new TextContent(resultText));
					return CallToolResult.builder()
						.content(content)
						.isError(false)
						.build();
				}
				
				Instrument instrument = frame.getCurrentInstrument();
				if (instrument == null) {
					Map<String, Object> response = new HashMap<>();
					response.put("error", "No instrument is set for the current virtual book");
					String resultText = jsonMapper.writeValueAsString(response);
					List<Content> content = List.of(new TextContent(resultText));
					return CallToolResult.builder()
						.content(content)
						.isError(false)
						.build();
				}
				
				logger.info("Extracting features directly from virtual book...");
				
				// Extract features directly from VirtualBook using native feature extractors
				VirtualBookFeatureExtractorManager featureManager = new VirtualBookFeatureExtractorManager();
				Map<String, Object> features = featureManager.extractAllFeatures(virtualBook);
				
				// Add metadata about the book
				Map<String, Object> result = new HashMap<>();
				result.put("features", Map.of(
					"extractedFeatures", features,
					"featureCount", features.size(),
					"extractionMethod", "Native VirtualBook Feature Extraction"
				));
				result.put("bookInfo", createBookInfo(virtualBook, instrument));
				result.put("recommendations", generateRecommendations(features));
				
				String resultText = jsonMapper.writeValueAsString(result);
				List<Content> content = List.of(new TextContent(resultText));
				
				return CallToolResult.builder()
					.content(content)
					.isError(false)
					.build();
					
			} catch (Exception e) {
				logger.error("Error extracting tune features: " + e.getMessage(), e);
				try {
					Map<String, Object> errorInfo = new HashMap<>();
					errorInfo.put("error", e.getMessage());
					errorInfo.put("type", e.getClass().getSimpleName());
					errorInfo.put("message", "Failed to extract features from virtual book.");
					String errorText = jsonMapper.writeValueAsString(errorInfo);
					List<Content> content = List.of(new TextContent(errorText));
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
	
	/**
	 * Create book information map.
	 */
	private static Map<String, Object> createBookInfo(VirtualBook virtualBook, Instrument instrument) {
		Map<String, Object> info = new HashMap<>();
		info.put("holeCount", virtualBook.getOrderedHolesCopy().size());
		info.put("length", virtualBook.getLength());
		info.put("scale", virtualBook.getScale() != null ? virtualBook.getScale().toString() : "null");
		if (virtualBook.getScale() != null) {
			info.put("trackCount", virtualBook.getScale().getTrackNb());
		}
		info.put("instrument", instrument != null ? instrument.getName() : "null");
		return info;
	}
	
	/**
	 * Generate recommendations based on extracted features.
	 */
	private static List<String> generateRecommendations(Map<String, Object> features) {
		java.util.List<String> recommendations = new java.util.ArrayList<>();
		
		// Analyze pitch range
		Object pitchRangeObj = features.get("Pitch Range");
		if (pitchRangeObj != null) {
			try {
				double pitchRange = Double.parseDouble(pitchRangeObj.toString());
				if (pitchRange < 12) {
					recommendations.add("Consider expanding the pitch range for more melodic variety");
				} else if (pitchRange > 60) {
					recommendations.add("Pitch range is very wide - consider focusing on a smaller range for better coherence");
				}
			} catch (NumberFormatException e) {
				// Ignore
			}
		}
		
		// Analyze rhythmic variability
		Object rhythmicVariabilityObj = features.get("Rhythmic Variability");
		if (rhythmicVariabilityObj != null) {
			try {
				double rhythmicVariability = Double.parseDouble(rhythmicVariabilityObj.toString());
				if (rhythmicVariability < 0.3) {
					recommendations.add("Low rhythmic variability detected - consider adding more rhythmic diversity");
				}
			} catch (NumberFormatException e) {
				// Ignore
			}
		}
		
		// Analyze polyphony
		Object polyphonicFractionObj = features.get("Polyphonic Fraction");
		if (polyphonicFractionObj != null) {
			try {
				double polyphonicFraction = Double.parseDouble(polyphonicFractionObj.toString());
				if (polyphonicFraction < 0.1) {
					recommendations.add("Tune is mostly monophonic - consider adding harmony or counterpoint");
				} else if (polyphonicFraction > 0.9) {
					recommendations.add("Very polyphonic - consider simplifying for clarity");
				}
			} catch (NumberFormatException e) {
				// Ignore
			}
		}
		
		// Analyze note count
		Object totalNotesObj = features.get("Total Number of Notes");
		if (totalNotesObj != null) {
			try {
				double totalNotes = Double.parseDouble(totalNotesObj.toString());
				if (totalNotes < 10) {
					recommendations.add("Very few notes detected - tune may be too sparse");
				} else if (totalNotes > 1000) {
					recommendations.add("Many notes detected - consider simplifying for better readability");
				}
			} catch (NumberFormatException e) {
				// Ignore
			}
		}
		
		if (recommendations.isEmpty()) {
			recommendations.add("No specific recommendations based on current analysis");
		}
		
		return recommendations;
	}
}

