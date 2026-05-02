package org.barrelorgandiscovery.gui.swing;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

/**
 * Installs {@link SafeFlatComboBoxUI} on FlatLaf combo boxes under a container.
 */
public final class ComboBoxPopupSafeguards {

	private ComboBoxPopupSafeguards() {
	}

	/**
	 * Walks {@code root} and installs the deferred popup UI on every
	 * {@link JComboBox} that uses {@link com.formdev.flatlaf.ui.FlatComboBoxUI}.
	 * Schedules work on the EDT after the current layout pass so newly added
	 * components are included.
	 */
	public static void installDeferredFlatComboPopupOnTreeLater(Container root) {
		if (root == null) {
			return;
		}
		SwingUtilities.invokeLater(() -> installOnSubtree(root));
	}

	private static void installOnSubtree(Component c) {
		if (c instanceof JComboBox) {
			SafeFlatComboBoxUI.installIfFlatLaf((JComboBox<?>) c);
		}
		if (c instanceof Container) {
			for (Component child : ((Container) c).getComponents()) {
				installOnSubtree(child);
			}
		}
	}
}
