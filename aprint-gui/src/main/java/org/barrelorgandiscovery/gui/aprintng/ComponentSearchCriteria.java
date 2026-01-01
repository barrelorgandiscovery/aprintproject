package org.barrelorgandiscovery.gui.aprintng;

/**
 * Criteria for searching Swing components.
 * 
 * @author APrint Development Team
 */
public class ComponentSearchCriteria {
	
	private String name;
	private String type;
	private String actionCommand;
	private String text;
	
	public ComponentSearchCriteria() {
	}
	
	public ComponentSearchCriteria(String name, String type, String actionCommand, String text) {
		this.name = name;
		this.type = type;
		this.actionCommand = actionCommand;
		this.text = text;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getActionCommand() {
		return actionCommand;
	}
	
	public void setActionCommand(String actionCommand) {
		this.actionCommand = actionCommand;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
}

