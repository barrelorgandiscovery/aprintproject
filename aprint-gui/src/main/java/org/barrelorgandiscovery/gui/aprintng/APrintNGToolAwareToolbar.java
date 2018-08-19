package org.barrelorgandiscovery.gui.aprintng;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;

/**
 * Toolbar managing the active tool button state
 * 
 * @author Freydiere Patrice
 * 
 */
public class APrintNGToolAwareToolbar extends JToolBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -488865939137990066L;

	private static Logger logger = Logger
			.getLogger(APrintNGToolAwareToolbar.class);

	private JEditableVirtualBookComponent ec;

	public APrintNGToolAwareToolbar(String name,
			JEditableVirtualBookComponent comp) {
		super(name);
		this.ec = comp;

		assert comp != null;

		// add tool change listener
		comp.addCurrentToolChangedListener(new CurrentToolChanged() {

			public void currentToolChanged(Tool oldTool, Tool newTool) {
				logger.debug("check toolbar button states");
				int cc = getComponentCount();
				for (int i = 0; i < cc; i++) {
					Component component = getComponentAtIndex(i);
					if (component instanceof ToolButton) {
						ToolButton tb = (ToolButton) component;
						tb.checkToolState();
					}
				}
			}
		});

	}

	public class ToolButton extends JToggleButton implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1485687468499659492L;

		/**
		 * Current selected Tool
		 */
		private Tool tool;

		/**
		 * Constructor
		 * 
		 * @param label
		 *            button label
		 * @param t
		 *            tool
		 */
		public ToolButton(String label, Tool t) {
			super(label);
			this.tool = t;
			addActionListener(this);
		}

		/**
		 * Get the tool
		 * 
		 * @return
		 */
		public Tool getTool() {
			return tool;
		}

		/**
		 * Check the state of the buttons / tools
		 */
		public void checkToolState() {
			Tool currentTool = ec.getCurrentTool();
			setSelected(currentTool == tool);
		}

		public void actionPerformed(ActionEvent e) {

			if (ec.getCurrentTool() == tool) {
				// reset the tool
				ec.setCurrentTool(null);
			} else {
				logger.debug("action performed, selected state :" + isSelected()); //$NON-NLS-1$
				ec.setCurrentTool(tool);
				logger.debug("select"); //$NON-NLS-1$
			}
		}

		@Override
		public synchronized void addMouseListener(MouseListener l) {
			super.addMouseListener(new NonDoubleClickMouseListener(l));
		}

	}

	public ToolButton addTool(Tool tool, String name) {
		return addTool(tool, name, null);
	}

	public ToolButton addTool(Tool tool, String name, Icon image) {
		return addTool(tool, name, image, null);
	}

	public ToolButton addTool(Tool tool, String name, Icon image, String toolTip) {
		ToolButton toolButton = new ToolButton(name, tool);

		if (image != null)
			toolButton.setIcon(image);

		if (toolTip != null)
			toolButton.setToolTipText(toolTip);

		add(toolButton);

		return toolButton;
	}

	public void addButton(ActionListener a, String name) {
		addButton(a, name, null);
	}

	public JButton addButton(ActionListener a, String name, Icon image) {
		JButton btn = new JButton(name);
		btn.addActionListener(a);
		if (image != null)
			btn.setIcon(image);
		add(btn);
		return btn;
	}

	/*
	 * private void disableButtonStates() {
	 * 
	 * int cc = getComponentCount(); for (int i = 0; i < cc; i++) { Component c
	 * = getComponentAtIndex(i); if (c instanceof ToolButton) { ((ToolButton)
	 * c).checkToolState(); } }
	 * 
	 * }
	 */

	private static class NonDoubleClickMouseListener implements MouseListener {
		private MouseListener inner;

		public NonDoubleClickMouseListener(MouseListener inner) {
			this.inner = inner;
		}

		public void mouseClicked(MouseEvent e) {

			MouseEvent newe = new MouseEvent(e.getComponent(), e.getID(),
					e.getWhen(), e.getModifiers(), e.getX(), e.getY(),/*
																	 * e.
																	 * getXOnScreen
																	 * (), e.
																	 * getYOnScreen
																	 * (),
																	 */1,
					e.isPopupTrigger(), e.getButton());

			inner.mouseClicked(newe);

		}

		public void mouseEntered(MouseEvent e) {
			inner.mouseEntered(e);
		}

		public void mouseExited(MouseEvent e) {
			inner.mouseExited(e);
		}

		long last = -1;

		public void mousePressed(MouseEvent e) {

			long millis = System.currentTimeMillis() - last;

			if (millis < 1000) {
				// nothing

			} else {
				inner.mousePressed(e);

			}

			last = System.currentTimeMillis();

		}

		public void mouseReleased(MouseEvent e) {
			inner.mouseReleased(e);
		}

	}

}
