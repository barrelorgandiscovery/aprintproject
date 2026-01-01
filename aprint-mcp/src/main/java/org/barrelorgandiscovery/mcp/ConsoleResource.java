package org.barrelorgandiscovery.mcp;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;

import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;

/**
 * Represents a console resource that can be tracked via MCP resources.
 * 
 * @author APrint Development Team
 */
public class ConsoleResource {
	
	private final String scriptName;
	private final String windowId;
	private final APrintGroovyConsolePanel console;
	private final JDialog dialog;
	private boolean readonly;
	private boolean hasUnsavedChanges;
	private long lastExecutionTime;
	private Object lastExecutionResult;
	private final long openedAt;
	
	public ConsoleResource(String scriptName, String windowId, 
	                       APrintGroovyConsolePanel console, JDialog dialog) {
		this.scriptName = scriptName != null ? scriptName : "temp";
		this.windowId = windowId;
		this.console = console;
		this.dialog = dialog;
		this.readonly = false;
		this.hasUnsavedChanges = false;
		this.lastExecutionTime = 0;
		this.lastExecutionResult = null;
		this.openedAt = System.currentTimeMillis();
	}
	
	public String getScriptName() {
		return scriptName;
	}
	
	public String getWindowId() {
		return windowId;
	}
	
	public APrintGroovyConsolePanel getConsole() {
		return console;
	}
	
	public JDialog getDialog() {
		return dialog;
	}
	
	public boolean isReadonly() {
		return readonly;
	}
	
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	
	public boolean hasUnsavedChanges() {
		return hasUnsavedChanges;
	}
	
	public void markAsModified() {
		this.hasUnsavedChanges = true;
	}
	
	public void clearModified() {
		this.hasUnsavedChanges = false;
	}
	
	public long getLastExecutionTime() {
		return lastExecutionTime;
	}
	
	public Object getLastExecutionResult() {
		return lastExecutionResult;
	}
	
	public void setLastExecution(long executionTime, Object result) {
		this.lastExecutionTime = executionTime;
		this.lastExecutionResult = result;
	}
	
	public long getOpenedAt() {
		return openedAt;
	}
	
	public boolean isOpen() {
		return dialog != null && dialog.isVisible();
	}
	
	/**
	 * Convert to JSON representation
	 */
	public String toJson() {
		Map<String, Object> map = new HashMap<>();
		map.put("windowId", windowId);
		map.put("scriptName", scriptName);
		map.put("status", isOpen() ? "open" : "closed");
		map.put("isReadonly", readonly);
		map.put("hasUnsavedChanges", hasUnsavedChanges);
		map.put("lastExecutionTime", lastExecutionTime > 0 ? lastExecutionTime : null);
		map.put("lastExecutionResult", lastExecutionResult != null ? lastExecutionResult.toString() : null);
		map.put("openedAt", openedAt);
		
		// Get current script content
		String scriptContent = console != null ? console.getScriptContent() : "";
		map.put("scriptContent", scriptContent);
		
		// Get output preview (last 200 chars)
		// Note: This would require access to the console output area
		// For now, we'll leave it empty or implement later
		map.put("outputPreview", "");
		
		// Simple JSON serialization (could use Gson if available)
		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"windowId\":\"").append(windowId).append("\",");
		json.append("\"scriptName\":\"").append(scriptName).append("\",");
		json.append("\"status\":\"").append(isOpen() ? "open" : "closed").append("\",");
		json.append("\"isReadonly\":").append(readonly).append(",");
		json.append("\"hasUnsavedChanges\":").append(hasUnsavedChanges).append(",");
		json.append("\"lastExecutionTime\":").append(lastExecutionTime > 0 ? lastExecutionTime : "null").append(",");
		json.append("\"lastExecutionResult\":").append(lastExecutionResult != null ? "\"" + lastExecutionResult.toString().replace("\"", "\\\"") + "\"" : "null").append(",");
		json.append("\"openedAt\":").append(openedAt).append(",");
		json.append("\"scriptContent\":\"").append(scriptContent.replace("\"", "\\\"").replace("\n", "\\n")).append("\",");
		json.append("\"outputPreview\":\"\"");
		json.append("}");
		
		return json.toString();
	}
}

