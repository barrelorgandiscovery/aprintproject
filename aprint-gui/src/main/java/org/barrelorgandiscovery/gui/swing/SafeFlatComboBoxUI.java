package org.barrelorgandiscovery.gui.swing;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComboBoxUI;

import com.formdev.flatlaf.ui.FlatComboBoxUI;

/**
 * Avoids {@code IllegalComponentStateException} from {@code JPopupMenu.show} when
 * FlatLaf tries to open a combo popup before the combo is {@code isShowing()}
 * (nested tabs, split panes, scroll viewports). Defers showing the popup until
 * the next EDT pass so layout and peer visibility catch up.
 */
public class SafeFlatComboBoxUI extends FlatComboBoxUI {

	@Override
	public void setPopupVisible(JComboBox<?> c, boolean visible) {
		if (visible && (c == null || !c.isShowing())) {
			SwingUtilities.invokeLater(() -> {
				if (c != null && c.isShowing()) {
					super.setPopupVisible(c, true);
				}
			});
			return;
		}
		super.setPopupVisible(c, visible);
	}

	/**
	 * Replaces a FlatLaf combo UI with this delegate; no-op if the current UI is
	 * not {@link FlatComboBoxUI} or is already safe.
	 */
	public static void installIfFlatLaf(JComboBox<?> combo) {
		if (combo == null) {
			return;
		}
		ComboBoxUI ui = combo.getUI();
		if (ui instanceof SafeFlatComboBoxUI) {
			return;
		}
		if (ui instanceof FlatComboBoxUI) {
			combo.setUI(new SafeFlatComboBoxUI());
		}
	}
}
