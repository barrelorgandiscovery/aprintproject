package org.barrelorgandiscovery.gui.aedit.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.barrelorgandiscovery.gui.aedit.CreationTool;
import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.aedit.UndoStack;
import org.barrelorgandiscovery.gui.aedit.ZoomBox;
import org.barrelorgandiscovery.gui.aedit.snapping.ISnappingEnvironment;
import org.barrelorgandiscovery.gui.aedit.snapping.SnappingEnvironmentHelper;
import org.barrelorgandiscovery.gui.aprint.MeasureTool;
import org.barrelorgandiscovery.gui.aprint.PanTool;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * toolbar for editing a virtualbook, handling tools activation and button
 * 
 * @author pfreydiere
 * 
 */
public class JVBToolingToolbar extends JToolBar {

	private JEditableVirtualBookComponent evb;

	private HashMap<Tool, JToggleButton> toolSet = new HashMap<Tool, JToggleButton>();

	private JButton zoommoins;

	private JButton zoomplus;

	private JToggleButton measure;

	public JVBToolingToolbar(JEditableVirtualBookComponent evb, UndoStack us, ISnappingEnvironment se)
			throws Exception {
		assert evb != null;
		this.evb = evb;

		// synchronize toolbar activation display
		evb.addCurrentToolChangedListener(new CurrentToolChanged() {

			public void currentToolChanged(Tool oldTool, Tool newTool) {

				JToggleButton b = toolSet.get(newTool);

				if (b != null) {
					b.setSelected(true);
				}

				b = toolSet.get(oldTool);
				if (b != null)
					b.setSelected(false);

			}
		});

		// add default tools

		JToggleButton pan = addTool(new PanTool(evb));
		pan.setIcon(new ImageIcon(getClass().getResource("hand.png"), org.barrelorgandiscovery.gui.aedit.toolbar.Messages.getString("JVBToolingToolbar.0"))); //$NON-NLS-1$ //$NON-NLS-2$
		pan.setToolTipText("Move the book"); //$NON-NLS-1$

		measure = addTool(new MeasureTool(evb));
		measure.setIcon(new ImageIcon(getClass().getResource("measure.png"), org.barrelorgandiscovery.gui.aedit.toolbar.Messages.getString("JVBToolingToolbar.1"))); //$NON-NLS-1$ //$NON-NLS-2$
		measure.setToolTipText(org.barrelorgandiscovery.gui.aedit.toolbar.Messages.getString("JVBToolingToolbar.2")); //$NON-NLS-1$

		zoomplus = add(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JEditableVirtualBookComponent pianoroll = JVBToolingToolbar.this.evb;
				pianoroll.setXfactor(pianoroll.getXfactor() / 2);
				pianoroll.repaint();

			}
		});
		zoomplus.setIcon(new ImageIcon(getClass().getResource("viewmagplus.png"), Messages.getString("APrint.95"))); //$NON-NLS-1$ //$NON-NLS-2$
		zoomplus.setToolTipText(Messages.getString("APrint.140")); //$NON-NLS-1$

		zoommoins = add(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JEditableVirtualBookComponent pianoroll = JVBToolingToolbar.this.evb;
				pianoroll.setXfactor(pianoroll.getXfactor() * 2);
				pianoroll.repaint();

			}
		});

		zoommoins.setIcon(new ImageIcon(getClass().getResource("viewmagminus.png"), org.barrelorgandiscovery.gui.aedit.toolbar.Messages.getString("JVBToolingToolbar.3"))); //$NON-NLS-1$ //$NON-NLS-2$
		zoommoins.setToolTipText(org.barrelorgandiscovery.gui.aedit.toolbar.Messages.getString("JVBToolingToolbar.4")); //$NON-NLS-1$

		JToggleButton zoombox = addTool(new ZoomBox(JVBToolingToolbar.this.evb));
		zoombox.setIcon(new ImageIcon(getClass().getResource("viewmagbox.png"))); //$NON-NLS-1$

		JButton fitToHeight = add(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JEditableVirtualBookComponent pianoroll = JVBToolingToolbar.this.evb;
				pianoroll.fitToScreen();
				pianoroll.repaint();

			}
		});

		fitToHeight.setIcon(new ImageIcon(getClass().getResource("viewmagfit.png"), org.barrelorgandiscovery.gui.aedit.toolbar.Messages.getString("JVBToolingToolbar.5"))); //$NON-NLS-1$ //$NON-NLS-2$
		fitToHeight.setToolTipText(org.barrelorgandiscovery.gui.aedit.toolbar.Messages.getString("JVBToolingToolbar.6")); //$NON-NLS-1$

		addSeparator();

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
		tb.setAction(new EVBToolAction(tool, evb));
		add(tb);

		toolSet.put(tool, tb);
		return tb;
	}

}
