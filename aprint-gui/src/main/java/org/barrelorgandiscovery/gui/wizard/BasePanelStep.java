package org.barrelorgandiscovery.gui.wizard;

import java.awt.LayoutManager;

import javax.swing.Icon;
import javax.swing.JPanel;

public abstract class BasePanelStep extends JPanel implements Step {

	protected String id;
	
	private Step parent;

	private String details;
	
	public BasePanelStep(String id, Step parent) {
		super();
		assert id != null;
		this.id = id;
		this.parent = parent;
	}

	public void setDetails(String details) {
		this.details = details;
	}
	
	public String getDetails() {
		return details;
	}
	
	public BasePanelStep(LayoutManager layout) {
		super(layout);
	}

	public BasePanelStep(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public BasePanelStep(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public Step getParentStep() {
		return parent;
	}

	public void setParentStep(Step parent) {
		this.parent = parent;
	}

	public String getId() {
		return this.id;
	}
	
	@Override
	public Icon getPageImage() {
		return null;
	}
	
}