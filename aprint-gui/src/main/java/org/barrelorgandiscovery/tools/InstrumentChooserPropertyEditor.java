package org.barrelorgandiscovery.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.barrelorgandiscovery.gui.aprint.instrumentchoice.JInstrumentChoice;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

public class InstrumentChooserPropertyEditor extends AbstractPropertyEditor {

	private Repository2 repository2;

	private JLabel scaleName;

	private JButton button;

	private JButton cancelButton;

	private Instrument scale;

	public InstrumentChooserPropertyEditor(Repository2 r) {
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
				selectInstrument();
			}
		});
		((JPanel) editor).add(cancelButton = ComponentFactory.Helper
				.getFactory().createMiniButton());
		cancelButton.setText("X");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNullInstrument();
			}
		});
	}

	private void selectInstrument() {

		JInstrumentChoice instrumentChoice = new JInstrumentChoice(repository2, null);
		instrumentChoice.setVisible(true);
		
		changeInstrument(instrumentChoice.getCurrentInstrument());

	}

	private void selectNullInstrument() {
		Object oldImage = getValue();
		scale = null;
		changeInstrument(null);
		firePropertyChange(oldImage, null);
	}

	private void changeInstrument(Instrument scale) {
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
			changeInstrument((Instrument) value);
			scale = (Instrument) value;
		} else {
			selectNullInstrument();
		}
	}

	@Override
	public String getAsText() {
		return super.getAsText();
	}
}
