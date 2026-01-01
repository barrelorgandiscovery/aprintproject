package org.barrelorgandiscovery.gui.aprintng;

/**
 * Information about the currently active window (VirtualBookFrame or Script Console).
 * This class is used internally in aprint-gui to represent window information.
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
}

