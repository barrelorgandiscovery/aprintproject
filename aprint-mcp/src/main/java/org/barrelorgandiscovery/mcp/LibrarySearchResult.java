package org.barrelorgandiscovery.mcp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Résultat typé de {@link APrintMCPContext#searchIndexedBooks(String, int)}.
 */
public final class LibrarySearchResult {

	private final boolean success;
	private final String errorMessage;
	private final List<IndexedBookHit> hits;

	private LibrarySearchResult(boolean success, String errorMessage, List<IndexedBookHit> hits) {
		this.success = success;
		this.errorMessage = errorMessage;
		this.hits = hits != null ? List.copyOf(hits) : List.of();
	}

	public static LibrarySearchResult ok(List<IndexedBookHit> hits) {
		return new LibrarySearchResult(true, null, hits);
	}

	public static LibrarySearchResult failure(String message) {
		return new LibrarySearchResult(false, message, List.of());
	}

	public boolean isSuccess() {
		return success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public List<IndexedBookHit> getHits() {
		return hits;
	}

	public int getCount() {
		return hits.size();
	}

	public Map<String, Object> toMap() {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("success", success);
		if (!success && errorMessage != null) {
			m.put("error", errorMessage);
		}
		m.put("hits", hits.stream().map(IndexedBookHit::toMap).collect(Collectors.toList()));
		m.put("count", hits.size());
		return m;
	}
}
