package org.barrelorgandiscovery.mcp;

import java.util.HashMap;
import java.util.Map;

/**
 * Information about the currently active window (VirtualBookFrame or Script Console).
 * 
 * @author APrint Development Team
 */
public class ActiveWindowInfo {
	
	public enum WindowType {
		VIRTUAL_BOOK_FRAME,
		SCRIPT_CONSOLE,
		UNKNOWN
	}
	
	private final WindowType type;
	private final String windowId;
	private final String title;
	private final String frameId; // For VirtualBookFrame
	private final String resourceUri; // For Script Console
	
	public ActiveWindowInfo(WindowType type, String windowId, String title, String frameId, String resourceUri) {
		this.type = type;
		this.windowId = windowId;
		this.title = title;
		this.frameId = frameId;
		this.resourceUri = resourceUri;
	}
	
	public WindowType getType() {
		return type;
	}
	
	public String getWindowId() {
		return windowId;
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
		map.put("type", type.name());
		map.put("windowId", windowId);
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

