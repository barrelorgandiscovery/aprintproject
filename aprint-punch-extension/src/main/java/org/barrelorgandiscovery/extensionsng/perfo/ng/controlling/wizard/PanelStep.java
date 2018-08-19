package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling.wizard;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;

/**
 * Wrapper for embed a panel into a wizard step
 * 
 * @author pfreydiere
 * 
 */
public class PanelStep extends JPanel implements Step  {

	private JPanel pagePanel;
	private String id;
	private String label;
	private String details;
	private Icon icon;
	
	public PanelStep(JPanel pagePanel, String id, String label, String details, Icon icon) {
		assert pagePanel != null;
		this.pagePanel = pagePanel;
		
		assert id != null;
		this.id = id;
		
		assert label != null;
		this.label = label;
		
		assert details != null;
		this.details = details;
		
		this.icon = icon; // may be null
		
		setLayout(new BorderLayout());
		add(this.pagePanel, BorderLayout.CENTER);
		
	}
	
	@Override
	public void activate(Serializable state, WizardStates allStepsStates,
			StepStatusChangedListener stepListener) throws Exception {

	}

	@Override
	public String getDetails() {
		return details;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	private Step parent = null;
	
	@Override
	public Step getParentStep() {
		return parent;
	}
	@Override
	public void setParentStep(Step parent) {
		this.parent = parent;
	}

	@Override
	public boolean isStepCompleted() {
		return true;
	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {
		return null;
	}

	@Override
	public Icon getPageImage() {
		return icon;
	}
	
}
