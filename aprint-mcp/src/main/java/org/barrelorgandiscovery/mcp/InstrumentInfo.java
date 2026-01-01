package org.barrelorgandiscovery.mcp;

import java.util.HashMap;
import java.util.Map;

/**
 * Information about an instrument for MCP.
 * 
 * @author APrint Development Team
 */
public class InstrumentInfo {
	
	private final String name;
	private final String scaleName;
	private final String descriptionUrl;
	private final boolean hasPicture;
	private final boolean hasMiniPicture;
	private final int trackNb; // Number of tracks in the scale
	
	public InstrumentInfo(String name, String scaleName, String descriptionUrl, 
	                      boolean hasPicture, boolean hasMiniPicture, int trackNb) {
		this.name = name;
		this.scaleName = scaleName;
		this.descriptionUrl = descriptionUrl;
		this.hasPicture = hasPicture;
		this.hasMiniPicture = hasMiniPicture;
		this.trackNb = trackNb;
	}
	
	public String getName() {
		return name;
	}
	
	public String getScaleName() {
		return scaleName;
	}
	
	public String getDescriptionUrl() {
		return descriptionUrl;
	}
	
	public boolean hasPicture() {
		return hasPicture;
	}
	
	public boolean hasMiniPicture() {
		return hasMiniPicture;
	}
	
	public int getTrackNb() {
		return trackNb;
	}
	
	/**
	 * Convert to a Map for JSON serialization
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("scaleName", scaleName);
		if (descriptionUrl != null) {
			map.put("descriptionUrl", descriptionUrl);
		}
		map.put("hasPicture", hasPicture);
		map.put("hasMiniPicture", hasMiniPicture);
		map.put("trackNb", trackNb);
		return map;
	}
}

