package org.barrelorgandiscovery.recognition.gui.booktoolbar;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;

/**
 * 
 * @author pfreydiere
 *
 */
public class EVBToolAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8476539274291245099L;

	
	private Tool t;

	private JEditableVirtualBookComponent display;

	public EVBToolAction(Tool t, JEditableVirtualBookComponent display) {
		assert t != null;
		assert display != null;

		this.t = t;
		this.display = display;
		
		
	}

	public void actionPerformed(ActionEvent e) {

		if (display.getCurrentTool() == t) {
			display.setCurrentTool(null);
		} else {
			display.setCurrentTool(t);
		}
	}
	
	
	

}
