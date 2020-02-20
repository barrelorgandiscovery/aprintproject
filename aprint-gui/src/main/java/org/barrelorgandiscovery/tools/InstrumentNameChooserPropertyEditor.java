package org.barrelorgandiscovery.tools;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.barrelorgandiscovery.gui.aprint.instrumentchoice.IInstrumentChoiceListener;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.JInstrumentChoice;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

public class InstrumentNameChooserPropertyEditor extends AbstractPropertyEditor {

	private Repository2 repository2;

	private JLabel instrumentName;

	private JButton button;

	private JButton cancelButton;

	private Instrument instrument;

	public InstrumentNameChooserPropertyEditor(Repository2 r) {
		super();

		repository2 = r;

		editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0)) {
			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				button.setEnabled(enabled);
				cancelButton.setEnabled(enabled);
			}
		};
		((JPanel) editor).add("*", instrumentName = new JLabel());
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

		JDialog f = new JDialog((Frame)null);
		
		JInstrumentChoice instrumentChoice = new JInstrumentChoice(repository2, 
				new IInstrumentChoiceListener() {					
					@Override
					public void instrumentChanged(Instrument newInstrument) {
						changeInstrument(newInstrument);
					}
				});

		f.getContentPane().add(instrumentChoice,  BorderLayout.CENTER);
		f.pack();
		f.setModalityType(ModalityType.APPLICATION_MODAL);
		f.setVisible(true);

	}

	private void selectNullInstrument() {
		Object oldImage = getValue();
		instrument = null;
		changeInstrument(null);
		firePropertyChange(oldImage, null);
	}

	private void changeInstrument(Instrument instrument) {
		Object oldValue = getValue();
		this.instrument = instrument;
		instrumentName.setText("");
		if (instrument != null)
			instrumentName.setText(instrument.getName());
		firePropertyChange(oldValue, instrument);
	}

	public Object getValue() {
		return instrument == null? null : instrument.getName();
	}

	public void setValue(Object value) {
		if (value instanceof Instrument) {
			changeInstrument((Instrument) value);
			instrument = (Instrument) value;
		} else if (value instanceof String) {
			changeInstrument(repository2.getInstrument((String)value));
		} else {
			selectNullInstrument();
		}
	}

	@Override
	public String getAsText() {
		return (String)getValue();
	}
}
