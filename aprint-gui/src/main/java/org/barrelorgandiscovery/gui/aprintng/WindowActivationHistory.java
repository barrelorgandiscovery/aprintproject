package org.barrelorgandiscovery.gui.aprintng;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages the history of window activations in the application.
 * Tracks which windows were activated, when, and maintains a history for MCP access.
 * 
 * @author APrint Development Team
 */
public class WindowActivationHistory {
	
	/**
	 * Represents a window activation event
	 */
	public static class ActivationEvent {
		private final long timestamp;
		private final String windowId;
		private final String windowType;
		private final String title;
		private final String frameId;
		private final String resourceUri;
		
		public ActivationEvent(long timestamp, String windowId, String windowType, 
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
	}
	
	// Maximum number of events to keep in history
	private static final int MAX_HISTORY_SIZE = 100;
	
	// History of window activations (most recent first)
	private final LinkedList<ActivationEvent> activationHistory = new LinkedList<>();
	
	// Current active window info
	private ActivationEvent currentActiveWindow = null;
	
	/**
	 * Record a window activation
	 */
	public synchronized void recordActivation(String windowId, String windowType, 
	                                           String title, String frameId, String resourceUri) {
		long timestamp = System.currentTimeMillis();
		ActivationEvent event = new ActivationEvent(timestamp, windowId, windowType, title, frameId, resourceUri);
		
		// Remove existing event for the same window if present (to avoid duplicates)
		activationHistory.removeIf(e -> e.getWindowId().equals(windowId));
		
		// Add new event at the beginning
		activationHistory.addFirst(event);
		
		// Limit history size
		while (activationHistory.size() > MAX_HISTORY_SIZE) {
			activationHistory.removeLast();
		}
		
		// Update current active window
		currentActiveWindow = event;
	}
	
	/**
	 * Get the current active window
	 */
	public synchronized ActivationEvent getCurrentActiveWindow() {
		return currentActiveWindow;
	}
	
	/**
	 * Get the activation history (most recent first)
	 * @param limit Maximum number of events to return (0 for all)
	 */
	public synchronized List<ActivationEvent> getHistory(int limit) {
		if (limit <= 0 || limit >= activationHistory.size()) {
			return new ArrayList<>(activationHistory);
		}
		return new ArrayList<>(activationHistory.subList(0, limit));
	}
	
	/**
	 * Get all activation history
	 */
	public synchronized List<ActivationEvent> getAllHistory() {
		return new ArrayList<>(activationHistory);
	}
	
	/**
	 * Get history for a specific window
	 */
	public synchronized List<ActivationEvent> getHistoryForWindow(String windowId) {
		List<ActivationEvent> result = new ArrayList<>();
		for (ActivationEvent event : activationHistory) {
			if (event.getWindowId().equals(windowId)) {
				result.add(event);
			}
		}
		return result;
	}
	
	/**
	 * Get the most recent activation for a specific window
	 */
	public synchronized ActivationEvent getMostRecentForWindow(String windowId) {
		for (ActivationEvent event : activationHistory) {
			if (event.getWindowId().equals(windowId)) {
				return event;
			}
		}
		return null;
	}
	
	/**
	 * Clear the history
	 */
	public synchronized void clear() {
		activationHistory.clear();
		currentActiveWindow = null;
	}
	
	/**
	 * Get the number of events in history
	 */
	public synchronized int size() {
		return activationHistory.size();
	}
}

