package org.barrelorgandiscovery.gui;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.HashCodeUtils;
import org.barrelorgandiscovery.tools.MidiHelper;


/**
 * 
 * Note Selector Panel
 * 
 * @author Freydiere Patrice
 */
public class JNoteSelectorPanel extends JPanel {

	private static Logger logger = Logger.getLogger(JNoteSelectorPanel.class);

	static class LocalizedNoteChoose {
		private int noteCode;
		private String localizedNote;

		public LocalizedNoteChoose(int noteCode) {
			this.noteCode = noteCode;
			this.localizedNote = MidiHelper.getLocalizedMidiNote(noteCode);

		}

		public int getNoteCode() {
			return noteCode;
		}

		@Override
		public String toString() {
			return localizedNote;
		}

		@Override
		public boolean equals(Object obj) {

			if (obj == null)
				return false;

			if (obj.getClass() == this.getClass()
					|| obj instanceof LocalizedNoteChoose) {
				return noteCode == ((LocalizedNoteChoose) obj).noteCode;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return HashCodeUtils.hash(HashCodeUtils.SEED, noteCode);
		}

	}

	private JSpinner spinnerOctave;

	private JComboBox comboRootKey;

	public JNoteSelectorPanel() {
		initComponents();
	}

	private void initComponents() {

		spinnerOctave = new JSpinner();
		spinnerOctave.setModel(new SpinnerNumberModel(4, 0, 10, 1));

		setLayout(new BorderLayout());
		add(spinnerOctave, BorderLayout.EAST);

		Vector<LocalizedNoteChoose> ln = new Vector<LocalizedNoteChoose>();
		for (int i = 0; i < 12; i++) {
			ln.add(new LocalizedNoteChoose(i));
		}

		comboRootKey = new JComboBox();

		DefaultComboBoxModel dcbm = new DefaultComboBoxModel(ln);
		comboRootKey.setModel(dcbm);
		comboRootKey.setMaximumRowCount(12);

		add(comboRootKey, BorderLayout.WEST);

	}

	public void setNote(int midinote) {
		int note = MidiHelper.extractNoteFromMidiCode(midinote);
		int octave = MidiHelper.getOctave(midinote);

		spinnerOctave.setValue(octave);

		DefaultComboBoxModel m = (DefaultComboBoxModel) comboRootKey.getModel();
		for (int i = 0; i < m.getSize(); i++) {
			LocalizedNoteChoose cn = (LocalizedNoteChoose) m.getElementAt(i);
			if (cn.getNoteCode() == note) {
				logger.debug("note found " + note); //$NON-NLS-1$
				comboRootKey.setSelectedItem(cn);
				break;
			}
		}

	}

	public int getNote() {
		LocalizedNoteChoose localizedSelectedNoteItem = (LocalizedNoteChoose) comboRootKey
				.getSelectedItem();

		return MidiHelper.computeMidiCodeFromNoteAndOctave(
				localizedSelectedNoteItem.getNoteCode(),
				(Integer) spinnerOctave.getValue());
	}

}
