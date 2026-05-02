package org.barrelorgandiscovery.mcp;

import java.util.Map;

/**
 * Paramètres typés pour {@link APrintMCPContext#searchIndexedBooks(String, int)} (outil search_library).
 */
public final class SearchLibraryRequest {

	private final String query;
	private final int maxResults;

	public SearchLibraryRequest(String query, int maxResults) {
		this.query = query != null ? query : "";
		this.maxResults = maxResults <= 0 ? 50 : Math.min(maxResults, 200);
	}

	public String getQuery() {
		return query;
	}

	public int getMaxResults() {
		return maxResults;
	}

	/**
	 * Parse les arguments MCP (types souvent Number / String).
	 */
	public static SearchLibraryRequest fromToolArguments(Map<String, Object> args) {
		if (args == null || !args.containsKey("query")) {
			throw new IllegalArgumentException("Missing required parameter: query");
		}
		String query = args.get("query") != null ? args.get("query").toString() : "";
		int max = 50;
		if (args.containsKey("maxResults") && args.get("maxResults") != null) {
			Object mr = args.get("maxResults");
			if (mr instanceof Number) {
				max = ((Number) mr).intValue();
			} else {
				max = Integer.parseInt(mr.toString());
			}
		}
		return new SearchLibraryRequest(query, max);
	}
}
