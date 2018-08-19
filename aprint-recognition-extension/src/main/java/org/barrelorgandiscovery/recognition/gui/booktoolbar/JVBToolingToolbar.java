package org.barrelorgandiscovery.recognition.gui.booktoolbar;

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
import org.barrelorgandiscovery.gui.aedit.snapping.ISnappingEnvironment;
import org.barrelorgandiscovery.gui.aedit.snapping.SnappingEnvironmentHelper;
import org.barrelorgandiscovery.gui.aprint.MeasureTool;
import org.barrelorgandiscovery.gui.aprint.PanTool;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * toolbar for editing a virtualbook
 * 
 * @author use
 * 
 */
public class JVBToolingToolbar extends JToolBar {

	private JEditableVirtualBookComponent evb;

	private HashMap<Tool, JToggleButton> toolSet = new HashMap<Tool, JToggleButton>();

	private JButton zoommoins;

	private JButton zoomplus;

	private JToggleButton measure;

	private JToggleButton creationtool;

	public JVBToolingToolbar(JEditableVirtualBookComponent evb, UndoStack us,
			ISnappingEnvironment se) throws Exception {
		assert evb != null;
		this.evb = evb;

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
		pan.setIcon(new ImageIcon(getClass().getResource("hand.png"), "Move")); //$NON-NLS-1$  
		pan.setToolTipText("Move the book"); //$NON-NLS-1$

		measure = addTool(new MeasureTool(evb));
		measure.setIcon(new ImageIcon(
				getClass().getResource("measure.png"), "Measure")); //$NON-NLS-1$  
		measure.setToolTipText("Measure on the book");

		zoomplus = add(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JEditableVirtualBookComponent pianoroll = JVBToolingToolbar.this.evb;
				pianoroll.setXfactor(pianoroll.getXfactor() / 2);
				pianoroll.repaint();

			}
		});
		zoomplus.setIcon(new ImageIcon(
				getClass().getResource("viewmag.png"), Messages.getString("APrint.95"))); //$NON-NLS-1$ //$NON-NLS-2$ 
		zoomplus.setToolTipText(Messages.getString("APrint.140")); //$NON-NLS-1$

		zoommoins = add(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JEditableVirtualBookComponent pianoroll = JVBToolingToolbar.this.evb;
				pianoroll.setXfactor(pianoroll.getXfactor() * 2);
				pianoroll.repaint();

			}
		});

		zoommoins.setIcon(new ImageIcon(getClass().getResource(
				"viewmagminus.png"), "Zoom +")); //$NON-NLS-1$  
		zoommoins.setToolTipText("Zoom on the book");

		JButton fitToHeight = add(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JEditableVirtualBookComponent pianoroll = JVBToolingToolbar.this.evb;
				pianoroll.fitToScreen();
				pianoroll.repaint();

			}
		});

		fitToHeight.setIcon(new ImageIcon(getClass().getResource(
				"viewmagfit.png"), "Fit")); //$NON-NLS-1$   
		fitToHeight.setToolTipText("Fit the book on the screen");

		
		BufferedImage b = ImageTools.loadImage(getClass().getResource("tablet.png"));
		
		addSeparator();
		creationtool = addTool(new CreationTool(evb, us, se));
		
		creationtool
				.setIcon(new ImageIcon(b));

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

	public JToggleButton getCreationtool() {
		return creationtool;
	}

}
