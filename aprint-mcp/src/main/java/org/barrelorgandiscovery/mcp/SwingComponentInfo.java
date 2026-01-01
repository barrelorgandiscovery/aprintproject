package org.barrelorgandiscovery.mcp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Information about a Swing component for MCP.
 * 
 * @author APrint Development Team
 */
public class SwingComponentInfo {
	
	private final String componentId;
	private final String componentPath;
	private final String className;
	private final String name;
	private final String text;
	private final boolean visible;
	private final boolean enabled;
	private final Map<String, Object> bounds;
	private final Map<String, Object> properties;
	private final List<String> childIds;
	private final String parentId;
	
	public SwingComponentInfo(String componentId, String componentPath, String className, 
	                          String name, String text, boolean visible, boolean enabled,
	                          Map<String, Object> bounds, Map<String, Object> properties,
	                          List<String> childIds, String parentId) {
		this.componentId = componentId;
		this.componentPath = componentPath;
		this.className = className;
		this.name = name;
		this.text = text;
		this.visible = visible;
		this.enabled = enabled;
		this.bounds = bounds;
		this.properties = properties != null ? properties : new HashMap<>();
		this.childIds = childIds;
		this.parentId = parentId;
	}
	
	public String getComponentId() {
		return componentId;
	}
	
	public String getComponentPath() {
		return componentPath;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getName() {
		return name;
	}
	
	public String getText() {
		return text;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public Map<String, Object> getBounds() {
		return bounds;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public List<String> getChildIds() {
		return childIds;
	}
	
	public String getParentId() {
		return parentId;
	}
	
	/**
	 * Convert to a Map for JSON serialization
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("componentId", componentId);
		map.put("componentPath", componentPath);
		map.put("className", className);
		if (name != null) {
			map.put("name", name);
		}
		if (text != null) {
			map.put("text", text);
		}
		map.put("visible", visible);
		map.put("enabled", enabled);
		if (bounds != null) {
			map.put("bounds", bounds);
		}
		if (!properties.isEmpty()) {
			map.put("properties", properties);
		}
		if (childIds != null && !childIds.isEmpty()) {
			map.put("childIds", childIds);
		}
		if (parentId != null) {
			map.put("parentId", parentId);
		}
		return map;
	}
}

