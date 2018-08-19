package org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.messages.Messages;

public class JViewingToolBar extends JToolBar {
	
	private static Logger logger = Logger.getLogger(JViewingToolBar.class);

	private JDisplay display;

	private HashMap<Tool, JToggleButton> toolSet = new HashMap<Tool, JToggleButton>();

	public JViewingToolBar(JDisplay display) throws Exception {

		assert display != null;

		this.display = display;

		display.addCurrentToolChangedListener(new CurrentToolChanged() {

			public void currentToolChanged(Tool oldTool, Tool newTool) {

				JToggleButton b = toolSet.get(newTool);

				if (b != null)
				{
					b.setSelected(true);
					
				}
				b = toolSet.get(oldTool);
				if (b != null)
					b.setSelected(false);

			}
		});

		JToggleButton b = addTool(new ZoomTool(display));

		b.setIcon(new ImageIcon(ZoomTool.class.getResource("viewmag.png"))); //$NON-NLS-1$
		b.setToolTipText(Messages.getString("JViewingToolBar.1")); //$NON-NLS-1$

		JButton bfit = add(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JViewingToolBar.this.display.fit();
			}
		});
		bfit.setIcon(new ImageIcon(ZoomTool.class.getResource("viewmagfit.png"))); //$NON-NLS-1$
		bfit.setToolTipText(Messages.getString("JViewingToolBar.3")); //$NON-NLS-1$
		
		
		JToggleButton tbpan = addTool(new PanTool(display));
		tbpan.setIcon(new ImageIcon(ZoomTool.class.getResource("hand.png"))); //$NON-NLS-1$
		// tbpan.setText("Pan");
	}

	/**
	 * add tool and return the button for specialize the button appearance
	 * 
	 * @param tool
	 * @return
	 */
	public JToggleButton addTool(Tool tool) {
		assert tool != null;
		
		JToggleButton tb = new JToggleButton();
		tb.setAction(new ToolAction(tool, display));
		add(tb);
		
		toolSet.put(tool, tb);
		return tb;
	}

}
