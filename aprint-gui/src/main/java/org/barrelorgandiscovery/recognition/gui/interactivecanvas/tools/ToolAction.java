package org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;

public class ToolAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8476539274291245099L;

	private Tool t;

	private JDisplay display;

	public ToolAction(Tool t, JDisplay display) {
		assert t != null;
		assert display != null;

		this.t = t;
		this.display = display;

	}

	public void actionPerformed(ActionEvent e) {
		if (display.getCurrentTool() == t) {
			display.resetCurrentTool();
		} else {
			display.setCurrentTool(t);
		}
	}

}
