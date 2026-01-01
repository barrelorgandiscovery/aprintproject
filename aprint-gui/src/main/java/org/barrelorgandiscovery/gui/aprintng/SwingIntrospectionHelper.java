package org.barrelorgandiscovery.gui.aprintng;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

/**
 * Helper class for introspecting Swing components.
 * Provides utilities to traverse component hierarchies and extract information.
 * 
 * @author APrint Development Team
 */
public class SwingIntrospectionHelper {
	
	private static final Logger logger = Logger.getLogger(SwingIntrospectionHelper.class);
	
	// Counter for generating unique component IDs
	private static final AtomicInteger componentIdCounter = new AtomicInteger(0);
	
	/**
	 * Generate a unique component ID
	 */
	private static String generateComponentId(Component component) {
		String name = component.getName();
		if (name != null && !name.isEmpty()) {
			return name + "_" + componentIdCounter.incrementAndGet();
		}
		return component.getClass().getSimpleName() + "_" + componentIdCounter.incrementAndGet();
	}
	
	/**
	 * Extract text from a component based on its type
	 */
	private static String extractText(Component component) {
		if (component instanceof JTextComponent) {
			return ((JTextComponent) component).getText();
		} else if (component instanceof JLabel) {
			return ((JLabel) component).getText();
		} else if (component instanceof AbstractButton) {
			return ((AbstractButton) component).getText();
		}
		return null;
	}
	
	/**
	 * Extract type-specific properties from a component
	 */
	private static Map<String, Object> extractProperties(Component component) {
		Map<String, Object> props = new HashMap<>();
		
		try {
			if (component instanceof AbstractButton) {
				AbstractButton btn = (AbstractButton) component;
				props.put("actionCommand", btn.getActionCommand());
				props.put("selected", btn.isSelected());
				if (btn instanceof javax.swing.JToggleButton) {
					props.put("toggleButton", true);
				}
			}
			
			if (component instanceof JTextComponent) {
				JTextComponent txt = (JTextComponent) component;
				props.put("editable", txt.isEditable());
				props.put("textLength", txt.getText().length());
			}
			
			if (component instanceof JComboBox) {
				JComboBox<?> combo = (JComboBox<?>) component;
				props.put("selectedIndex", combo.getSelectedIndex());
				props.put("selectedItem", combo.getSelectedItem() != null ? combo.getSelectedItem().toString() : null);
				props.put("itemCount", combo.getItemCount());
			}
			
			if (component instanceof JList) {
				JList<?> list = (JList<?>) component;
				props.put("selectedIndices", list.getSelectedIndices());
				props.put("selectedValues", list.getSelectedValuesList());
				props.put("modelSize", list.getModel().getSize());
			}
			
			if (component instanceof JTable) {
				JTable table = (JTable) component;
				props.put("rowCount", table.getRowCount());
				props.put("columnCount", table.getColumnCount());
				props.put("selectedRow", table.getSelectedRow());
				props.put("selectedColumn", table.getSelectedColumn());
			}
			
			if (component instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) component;
				props.put("actionCommand", item.getActionCommand());
				KeyStroke accel = item.getAccelerator();
				if (accel != null) {
					props.put("accelerator", accel.toString());
				}
			}
			
			// Try to get tooltip text
			try {
				Method getToolTipText = component.getClass().getMethod("getToolTipText");
				Object tooltip = getToolTipText.invoke(component);
				if (tooltip != null) {
					props.put("toolTipText", tooltip.toString());
				}
			} catch (Exception e) {
				// Ignore if method doesn't exist
			}
			
		} catch (Exception e) {
			logger.debug("Error extracting properties from component: " + component.getClass().getName(), e);
		}
		
		return props;
	}
	
	/**
	 * Convert a component to SwingComponentInfo
	 */
	public static SwingComponentInfo componentToInfo(Component component, String componentId, 
	                                                  String componentPath, String parentId) {
		if (component == null) {
			return null;
		}
		
		String className = component.getClass().getName();
		String name = component.getName();
		String text = extractText(component);
		boolean visible = component.isVisible();
		boolean enabled = component.isEnabled();
		Rectangle bounds = component.getBounds();
		Map<String, Object> properties = extractProperties(component);
		
		// Get child IDs if it's a container
		List<String> childIds = null;
		if (component instanceof Container) {
			Container container = (Container) component;
			childIds = new ArrayList<>();
			for (int i = 0; i < container.getComponentCount(); i++) {
				Component child = container.getComponent(i);
				String childId = generateComponentId(child);
				childIds.add(childId);
			}
		}
		
		return new SwingComponentInfo(componentId, componentPath, className, name, text,
		                              visible, enabled, bounds, properties, childIds, parentId);
	}
	
	/**
	 * Traverse component hierarchy and collect all components
	 */
	public static List<SwingComponentInfo> traverseComponents(Component root, String rootPath, 
	                                                           String filterType, int maxDepth) {
		List<SwingComponentInfo> result = new ArrayList<>();
		Map<Component, String> componentIds = new HashMap<>();
		Map<Component, String> componentPaths = new HashMap<>();
		
		traverseRecursive(root, rootPath, null, filterType, maxDepth, 0, result, componentIds, componentPaths);
		
		return result;
	}
	
	private static void traverseRecursive(Component component, String currentPath, String parentId,
	                                      String filterType, int maxDepth, int currentDepth,
	                                      List<SwingComponentInfo> result, 
	                                      Map<Component, String> componentIds,
	                                      Map<Component, String> componentPaths) {
		if (component == null || currentDepth > maxDepth) {
			return;
		}
		
		// Check filter
		if (filterType != null && !filterType.isEmpty()) {
			if (!component.getClass().getName().equals(filterType) && 
			    !component.getClass().getSimpleName().equals(filterType)) {
				// Don't add this component, but continue traversing children
				if (component instanceof Container) {
					Container container = (Container) component;
					for (int i = 0; i < container.getComponentCount(); i++) {
						Component child = container.getComponent(i);
						String childId = generateComponentId(child);
						String childPath = currentPath + "/" + childId;
						traverseRecursive(child, childPath, null, filterType, maxDepth, currentDepth + 1,
						                  result, componentIds, componentPaths);
					}
				}
				return;
			}
		}
		
		// Generate ID and path for this component
		String componentId = componentIds.get(component);
		if (componentId == null) {
			componentId = generateComponentId(component);
			componentIds.put(component, componentId);
		}
		
		String componentPath = componentPaths.get(component);
		if (componentPath == null) {
			componentPath = currentPath;
			componentPaths.put(component, componentPath);
		}
		
		// Create info for this component
		SwingComponentInfo info = componentToInfo(component, componentId, componentPath, parentId);
		result.add(info);
		
		// Traverse children
		if (component instanceof Container && currentDepth < maxDepth) {
			Container container = (Container) component;
			for (int i = 0; i < container.getComponentCount(); i++) {
				Component child = container.getComponent(i);
				String childId = generateComponentId(child);
				String childPath = componentPath + "/" + childId;
				traverseRecursive(child, childPath, componentId, filterType, maxDepth, currentDepth + 1,
				                  result, componentIds, componentPaths);
			}
		}
	}
	
	/**
	 * Find components matching criteria
	 */
	public static List<SwingComponentInfo> findComponents(Component root, String rootPath,
	                                                      ComponentSearchCriteria criteria) {
		List<SwingComponentInfo> allComponents = traverseComponents(root, rootPath, null, 20);
		List<SwingComponentInfo> matches = new ArrayList<>();
		
		for (SwingComponentInfo info : allComponents) {
			boolean matchesCriteria = true;
			
			if (criteria.getName() != null && !criteria.getName().isEmpty()) {
				if (!criteria.getName().equals(info.getName())) {
					matchesCriteria = false;
				}
			}
			
			if (criteria.getType() != null && !criteria.getType().isEmpty()) {
				if (!criteria.getType().equals(info.getClassName()) && 
				    !criteria.getType().equals(info.getClassName().substring(info.getClassName().lastIndexOf('.') + 1))) {
					matchesCriteria = false;
				}
			}
			
			if (criteria.getText() != null && !criteria.getText().isEmpty()) {
				if (info.getText() == null || !info.getText().contains(criteria.getText())) {
					matchesCriteria = false;
				}
			}
			
			if (criteria.getActionCommand() != null && !criteria.getActionCommand().isEmpty()) {
				Object actionCmd = info.getProperties().get("actionCommand");
				if (actionCmd == null || !criteria.getActionCommand().equals(actionCmd.toString())) {
					matchesCriteria = false;
				}
			}
			
			if (matchesCriteria) {
				matches.add(info);
			}
		}
		
		return matches;
	}
	
	/**
	 * Get component by path - improved version that uses consistent ID generation
	 */
	public static Component getComponentByPath(Component root, String path) {
		if (root == null || path == null || path.isEmpty()) {
			return root;
		}
		
		// Build a map of component IDs using the same logic as traverseComponents
		Map<Component, String> componentIds = new HashMap<>();
		buildComponentIdMap(root, componentIds);
		
		String[] parts = path.split("/");
		Component current = root;
		
		for (int i = 1; i < parts.length; i++) { // Skip first part (root path)
			if (!(current instanceof Container)) {
				return null;
			}
			
			Container container = (Container) current;
			String targetId = parts[i];
			
			// Try to find by ID first (from our map)
			Component found = null;
			for (int j = 0; j < container.getComponentCount(); j++) {
				Component child = container.getComponent(j);
				String childId = componentIds.get(child);
				if (childId == null) {
					childId = generateComponentId(child);
					componentIds.put(child, childId);
				}
				
				// Try multiple matching strategies
				if (targetId.equals(childId) || 
					targetId.equals(child.getName()) ||
					(targetId.contains("_") && childId.contains(targetId.split("_")[0]))) {
					found = child;
					break;
				}
			}
			
			if (found == null) {
				// Try to find by partial match (for cases with spaces or special chars)
				for (int j = 0; j < container.getComponentCount(); j++) {
					Component child = container.getComponent(j);
					String childId = componentIds.get(child);
					if (childId == null) {
						childId = generateComponentId(child);
						componentIds.put(child, childId);
					}
					
					// Partial match: check if targetId is contained in childId or vice versa
					if (childId.contains(targetId) || targetId.contains(childId)) {
						found = child;
						break;
					}
				}
			}
			
			if (found == null) {
				logger.debug("Component not found in path: " + targetId + " at level " + i + " of path: " + path);
				return null;
			}
			
			current = found;
		}
		
		return current;
	}
	
	/**
	 * Build a map of component IDs using the same logic as traverseComponents
	 */
	private static void buildComponentIdMap(Component component, Map<Component, String> componentIds) {
		if (component == null) {
			return;
		}
		
		String componentId = componentIds.get(component);
		if (componentId == null) {
			componentId = generateComponentId(component);
			componentIds.put(component, componentId);
		}
		
		if (component instanceof Container) {
			Container container = (Container) component;
			for (int i = 0; i < container.getComponentCount(); i++) {
				Component child = container.getComponent(i);
				buildComponentIdMap(child, componentIds);
			}
		}
	}
	
	/**
	 * Get component value based on its type
	 */
	public static Object getComponentValue(Component component) {
		if (component == null) {
			return null;
		}
		
		// Text components
		if (component instanceof JTextComponent) {
			return ((JTextComponent) component).getText();
		} else if (component instanceof JLabel) {
			return ((JLabel) component).getText();
		} 
		// Button components
		else if (component instanceof AbstractButton) {
			AbstractButton btn = (AbstractButton) component;
			if (btn instanceof JToggleButton) {
				return ((JToggleButton) btn).isSelected();
			}
			String text = btn.getText();
			// If no text, try to get tooltip or action command
			if (text == null || text.isEmpty()) {
				String tooltip = btn.getToolTipText();
				if (tooltip != null && !tooltip.isEmpty()) {
					return tooltip;
				}
				String action = btn.getActionCommand();
				if (action != null && !action.isEmpty()) {
					return action;
				}
			}
			return text;
		} 
		// Combo box
		else if (component instanceof JComboBox) {
			JComboBox<?> combo = (JComboBox<?>) component;
			Object selected = combo.getSelectedItem();
			if (selected != null) {
				return selected.toString();
			}
			return null;
		} 
		// List
		else if (component instanceof JList) {
			JList<?> list = (JList<?>) component;
			return list.getSelectedValuesList();
		} 
		// Table
		else if (component instanceof JTable) {
			JTable table = (JTable) component;
			int row = table.getSelectedRow();
			int col = table.getSelectedColumn();
			if (row >= 0 && col >= 0) {
				return table.getValueAt(row, col);
			}
			return null;
		}
		// Slider
		else if (component instanceof JSlider) {
			return ((JSlider) component).getValue();
		}
		// Progress bar
		else if (component instanceof JProgressBar) {
			return ((JProgressBar) component).getValue();
		}
		// Spinner
		else if (component instanceof JSpinner) {
			return ((JSpinner) component).getValue();
		}
		// Checkbox
		else if (component instanceof JCheckBox) {
			return ((JCheckBox) component).isSelected();
		}
		// Radio button
		else if (component instanceof JRadioButton) {
			return ((JRadioButton) component).isSelected();
		}
		// Tabbed pane - return selected index
		else if (component instanceof JTabbedPane) {
			return ((JTabbedPane) component).getSelectedIndex();
		}
		// For other components, try to extract text or return a meaningful representation
		else {
			// Try to get text via reflection or common methods
			String text = extractText(component);
			if (text != null && !text.isEmpty()) {
				return text;
			}
			// Return component name if available
			String name = component.getName();
			if (name != null && !name.isEmpty()) {
				return name;
			}
			// Last resort: return class name
			return component.getClass().getSimpleName();
		}
	}
	
	/**
	 * Get a specific property from a component
	 */
	public static Object getComponentProperty(Component component, String propertyName) {
		if (component == null || propertyName == null) {
			return null;
		}
		
		try {
			String propLower = propertyName.toLowerCase();
			
			// Try common properties first (case-insensitive)
			switch (propLower) {
				case "text":
					return extractText(component);
				case "name":
					return component.getName();
				case "visible":
					return component.isVisible();
				case "enabled":
					return component.isEnabled();
				case "class":
				case "classname":
					return component.getClass().getName();
				case "tooltip":
				case "tooltiptext":
					try {
						if (component instanceof JComponent) {
							return ((JComponent) component).getToolTipText();
						}
						Method getToolTipText = component.getClass().getMethod("getToolTipText");
						return getToolTipText.invoke(component);
					} catch (Exception e) {
						return null;
					}
				case "selected":
				case "isselected":
					if (component instanceof AbstractButton) {
						return ((AbstractButton) component).isSelected();
					}
					if (component instanceof JToggleButton) {
						return ((JToggleButton) component).isSelected();
					}
					break;
				case "selecteditem":
					if (component instanceof JComboBox) {
						Object item = ((JComboBox<?>) component).getSelectedItem();
						return item != null ? item.toString() : null;
					}
					break;
				case "selectedindex":
					if (component instanceof JComboBox) {
						return ((JComboBox<?>) component).getSelectedIndex();
					}
					if (component instanceof JTabbedPane) {
						return ((JTabbedPane) component).getSelectedIndex();
					}
					if (component instanceof JList) {
						return ((JList<?>) component).getSelectedIndex();
					}
					break;
				case "value":
					if (component instanceof JSlider) {
						return ((JSlider) component).getValue();
					}
					if (component instanceof JSpinner) {
						return ((JSpinner) component).getValue();
					}
					if (component instanceof JProgressBar) {
						return ((JProgressBar) component).getValue();
					}
					break;
				case "actioncommand":
					if (component instanceof AbstractButton) {
						return ((AbstractButton) component).getActionCommand();
					}
					break;
				case "title":
					if (component instanceof JFrame) {
						return ((JFrame) component).getTitle();
					}
					if (component instanceof JDialog) {
						return ((JDialog) component).getTitle();
					}
					if (component instanceof JInternalFrame) {
						return ((JInternalFrame) component).getTitle();
					}
					break;
			}
			
			// Try to get property via reflection with multiple naming conventions
			String[] methodNames = {
				"get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1),
				"is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1),
				"get" + propertyName.toUpperCase(),
				propertyName
			};
			
			for (String methodName : methodNames) {
				try {
					Method method = component.getClass().getMethod(methodName);
					Object result = method.invoke(component);
					if (result != null) {
						return result;
					}
				} catch (NoSuchMethodException e) {
					// Try next method name
					continue;
				} catch (Exception e) {
					logger.debug("Error invoking method " + methodName + " on component", e);
					// Continue to next method name
				}
			}
			
			// Try to access as field (less common, but sometimes useful)
			try {
				Field field = component.getClass().getField(propertyName);
				return field.get(component);
			} catch (NoSuchFieldException e) {
				// Field doesn't exist, that's ok
			} catch (Exception e) {
				logger.debug("Error accessing field " + propertyName + " on component", e);
			}
			
			return null;
		} catch (Exception e) {
			logger.debug("Error getting property " + propertyName + " from component", e);
			return null;
		}
	}
}

