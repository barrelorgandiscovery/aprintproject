package org.barrelorgandiscovery.gui.aprintng;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Tracks window usage to determine the most recently used window.
 * Uses a LinkedHashMap to maintain insertion order (most recent last).
 * 
 * @author APrint Development Team
 */
public class WindowUsageTracker {
	
	private static final Logger logger = Logger.getLogger(WindowUsageTracker.class);
	
	// Map of window -> last activation timestamp
	// Using LinkedHashMap with accessOrder=true to maintain LRU order
	private final Map<Window, Long> windowUsageTimes = new LinkedHashMap<Window, Long>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Window, Long> eldest) {
			// Keep only the last 100 windows to prevent memory leaks
			return size() > 100;
		}
	};
	
	/**
	 * Register a window for tracking
	 */
	public void registerWindow(Window window) {
		if (window == null) {
			return;
		}
		
		// Add window listener to track activation
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				markWindowUsed(e.getWindow());
			}
			
			@Override
			public void windowGainedFocus(WindowEvent e) {
				markWindowUsed(e.getWindow());
			}
		});
		
		// If window is already active, mark it as used
		if (window.isActive()) {
			markWindowUsed(window);
		}
	}
	
	/**
	 * Mark a window as recently used
	 */
	public void markWindowUsed(Window window) {
		if (window != null && window.isVisible()) {
			synchronized (windowUsageTimes) {
				windowUsageTimes.put(window, System.currentTimeMillis());
			}
			logger.debug("Marked window as used: " + window.getClass().getSimpleName());
		}
	}
	
	/**
	 * Get the most recently used window
	 */
	public Window getMostRecentlyUsedWindow() {
		synchronized (windowUsageTimes) {
			if (windowUsageTimes.isEmpty()) {
				return null;
			}
			
			// LinkedHashMap with accessOrder=true keeps most recently accessed last
			Window mostRecent = null;
			long mostRecentTime = 0;
			
			for (Map.Entry<Window, Long> entry : windowUsageTimes.entrySet()) {
				Window w = entry.getKey();
				Long time = entry.getValue();
				
				// Only consider visible windows
				if (w != null && w.isVisible() && time != null && time > mostRecentTime) {
					mostRecent = w;
					mostRecentTime = time;
				}
			}
			
			return mostRecent;
		}
	}
	
	/**
	 * Get all tracked windows ordered by most recent usage
	 */
	public java.util.List<Window> getAllWindowsOrderedByUsage() {
		synchronized (windowUsageTimes) {
			java.util.List<Window> result = new java.util.ArrayList<>();
			
			// Create a list sorted by timestamp (most recent first)
			java.util.List<Map.Entry<Window, Long>> entries = new java.util.ArrayList<>(windowUsageTimes.entrySet());
			entries.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));
			
			for (Map.Entry<Window, Long> entry : entries) {
				Window w = entry.getKey();
				if (w != null && w.isVisible()) {
					result.add(w);
				}
			}
			
			return result;
		}
	}
	
	/**
	 * Remove a window from tracking (when it's closed)
	 */
	public void unregisterWindow(Window window) {
		if (window != null) {
			synchronized (windowUsageTimes) {
				windowUsageTimes.remove(window);
			}
		}
	}
	
	/**
	 * Get usage time for a window
	 */
	public Long getUsageTime(Window window) {
		synchronized (windowUsageTimes) {
			return windowUsageTimes.get(window);
		}
	}
}

