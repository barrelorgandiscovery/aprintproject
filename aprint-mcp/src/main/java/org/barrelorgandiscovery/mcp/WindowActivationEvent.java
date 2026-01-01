package org.barrelorgandiscovery.mcp;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a window activation event for MCP.
 * 
 * @author APrint Development Team
 */
public class WindowActivationEvent {
	
	private final long timestamp;
	private final String windowId;
	private final String windowType;
	private final String title;
	private final String frameId;
	private final String resourceUri;
	
	public WindowActivationEvent(long timestamp, String windowId, String windowType, 
	                             String title, String frameId, String resourceUri) {
		this.timestamp = timestamp;
		this.windowId = windowId;
		this.windowType = windowType;
		this.title = title;
		this.frameId = frameId;
		this.resourceUri = resourceUri;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getWindowId() {
		return windowId;
	}
	
	public String getWindowType() {
		return windowType;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getFrameId() {
		return frameId;
	}
	
	public String getResourceUri() {
		return resourceUri;
	}
	
	/**
	 * Convert to a Map for JSON serialization
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("timestamp", timestamp);
		map.put("windowId", windowId);
		map.put("windowType", windowType);
		map.put("title", title);
		if (frameId != null) {
			map.put("frameId", frameId);
		}
		if (resourceUri != null) {
			map.put("resourceUri", resourceUri);
		}
		return map;
	}
}

