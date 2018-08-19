package org.barrelorgandiscovery.tools;

import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.ascale.ScaleChooserDialog;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

public class ScaleChooserPropertyEditor extends AbstractPropertyEditor {

	private Repository2 repository2;

	private JLabel scaleName;

	private JButton button;

	private JButton cancelButton;

	private Scale scale;

	public ScaleChooserPropertyEditor(Repository2 r) {
		super();

		repository2 = r;

		editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0)) {
			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				button.setEnabled(enabled);
				cancelButton.setEnabled(enabled);
			}
		};
		((JPanel) editor).add("*", scaleName = new JLabel());
		((JPanel) editor).add(button = ComponentFactory.Helper.getFactory()
				.createMiniButton());

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectScale();
			}
		});
		((JPanel) editor).add(cancelButton = ComponentFactory.Helper
				.getFactory().createMiniButton());
		cancelButton.setText("X");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNullScale();
			}
		});
	}

	private void selectScale() {

		ScaleChooserDialog scaleChooserDialog = new ScaleChooserDialog(
				repository2, (Frame) null, true);
		scaleChooserDialog.setVisible(true);
		changeScale(scaleChooserDialog.getSelectedScale());

	}

	private void selectNullScale() {
		Object oldImage = getValue();
		scale = null;
		changeScale(null);
		firePropertyChange(oldImage, null);
	}

	private void changeScale(Scale scale) {
		Object oldImage = getValue();
		this.scale = scale;
		scaleName.setText("");
		if (scale != null)
			scaleName.setText(scale.getName());
		firePropertyChange(oldImage, scale);
	}

	public Object getValue() {
		return scale;
	}

	public void setValue(Object value) {
		if (value instanceof Scale) {
			changeScale((Scale) value);
			scale = (Scale) value;
		} else {
			selectNullScale();
		}
	}

	@Override
	public String getAsText() {
		return super.getAsText();
	}
}
