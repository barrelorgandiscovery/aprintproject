package org.barrelorgandiscovery.mcp;

import java.util.HashMap;
import java.util.Map;

/**
 * Information about a scale (gamme) for MCP.
 * 
 * @author APrint Development Team
 */
public class ScaleInfo {
	
	private final String name;
	private final double width; // in mm
	private final int trackNb; // Number of tracks
	private final double speed; // in mm/s
	private final String informations; // Free text information
	private final String state; // Scale state (INPROGRESS, COMPLETED, etc.)
	private final String contact; // Contact email
	private final boolean bookMovingRightToLeft;
	
	public ScaleInfo(String name, double width, int trackNb, double speed, 
	                 String informations, String state, String contact, 
	                 boolean bookMovingRightToLeft) {
		this.name = name;
		this.width = width;
		this.trackNb = trackNb;
		this.speed = speed;
		this.informations = informations;
		this.state = state;
		this.contact = contact;
		this.bookMovingRightToLeft = bookMovingRightToLeft;
	}
	
	public String getName() {
		return name;
	}
	
	public double getWidth() {
		return width;
	}
	
	public int getTrackNb() {
		return trackNb;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public String getInformations() {
		return informations;
	}
	
	public String getState() {
		return state;
	}
	
	public String getContact() {
		return contact;
	}
	
	public boolean isBookMovingRightToLeft() {
		return bookMovingRightToLeft;
	}
	
	/**
	 * Convert to a Map for JSON serialization
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("width", width);
		map.put("trackNb", trackNb);
		map.put("speed", speed);
		if (informations != null) {
			map.put("informations", informations);
		}
		if (state != null) {
			map.put("state", state);
		}
		if (contact != null) {
			map.put("contact", contact);
		}
		map.put("bookMovingRightToLeft", bookMovingRightToLeft);
		return map;
	}
}

