package org.barrelorgandiscovery.mcp;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Une entrée d’index Lucene (recherche bibliothèque), typée.
 */
public final class IndexedBookHit {

	private final double score;
	private final String name;
	private final String scale;
	private final String instrument;
	private final String genre;
	private final String description;
	private final String fileref;

	public IndexedBookHit(double score, String name, String scale, String instrument, String genre,
			String description, String fileref) {
		this.score = score;
		this.name = name;
		this.scale = scale;
		this.instrument = instrument;
		this.genre = genre;
		this.description = description;
		this.fileref = fileref;
	}

	public double getScore() {
		return score;
	}

	public String getName() {
		return name;
	}

	public String getScale() {
		return scale;
	}

	public String getInstrument() {
		return instrument;
	}

	public String getGenre() {
		return genre;
	}

	public String getDescription() {
		return description;
	}

	public String getFileref() {
		return fileref;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("score", score);
		if (name != null) {
			m.put("name", name);
		}
		if (scale != null) {
			m.put("scale", scale);
		}
		if (instrument != null) {
			m.put("instrument", instrument);
		}
		if (genre != null) {
			m.put("genre", genre);
		}
		if (description != null) {
			m.put("description", description);
		}
		if (fileref != null) {
			m.put("fileref", fileref);
		}
		return m;
	}
}
