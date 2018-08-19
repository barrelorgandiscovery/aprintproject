package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.tools.HashCodeUtils;
import org.barrelorgandiscovery.tools.MidiHelper;

import com.jeta.forms.components.panel.FormPanel;

public class NoteDefComponent extends AbstractTrackDefComponent {

	private static final Logger logger = Logger
			.getLogger(NoteDefComponent.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -8811002736262470180L

	;
	private JLabel labelNote;
	private JComboBox note;
	private JLabel labeloctave;
	private JSpinner spinneroctave;

	private JLabel labelclassification;
	private JComboBox registerset;

	public NoteDefComponent() throws Exception {
		initComponent();
	}

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

	private void initComponent() throws Exception {

		labelNote = new JLabel(Messages.getString("NoteDefComponent.0")); //$NON-NLS-1$

		Vector<LocalizedNoteChoose> ln = new Vector<LocalizedNoteChoose>();
		for (int i = 0; i < 12; i++) {
			ln.add(new LocalizedNoteChoose(i));
		}

		note = new JComboBox(ln);
		note.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					sendNewNote();
				}
			}
		});
		note.setMaximumRowCount(12);

		labeloctave = new JLabel(Messages.getString("NoteDefComponent.1")); //$NON-NLS-1$
		spinneroctave = new JSpinner(new SpinnerNumberModel(4, 0, 10, 1));
		spinneroctave.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				sendNewNote();
			}
		});

		labelclassification = new JLabel(Messages
				.getString("NoteDefComponent.2")); //$NON-NLS-1$
		registerset = new JComboBox();
		registerset.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					sendNewNote();
				}
			}
		});

		FormPanel p = new FormPanel(getClass().getResourceAsStream(
				"noteDefComponent.jfrm"));
		p.getFormAccessor().replaceBean(p.getComponentByName("labelNote"),
				labelNote);
		p.getFormAccessor().replaceBean(p.getComponentByName("note"), note);
		p.getFormAccessor().replaceBean(p.getComponentByName("labeloctave"),
				labeloctave);
		p.getFormAccessor().replaceBean(p.getComponentByName("spinneroctave"),
				spinneroctave);
		p.getFormAccessor().replaceBean(
				p.getComponentByName("labelclassification"),
				labelclassification);
		p.getFormAccessor().replaceBean(p.getComponentByName("registerset"),
				registerset);
		
		JLabel music = new JLabel();
		music.setIcon(new ImageIcon(getClass().getResource("musicsheet.jpg")));
		p.getFormAccessor().replaceBean(p.getComponentByName("imgdescription"),
				music);

		//		
		// JPanel p = new JPanel();
		// p.setLayout(new GridBagLayout());
		//
		// p.add(labelNote, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
		// GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
		// 0, 0, 5, 5), 0, 0));
		// p.add(note, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
		// GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
		// 0, 0, 5, 5), 0, 0));
		//
		// p.add(labeloctave, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
		// GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
		// 0, 0, 5, 5), 0, 0));
		// p.add(spinneroctave, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
		// GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
		// 0, 0, 5, 5), 0, 0));
		//
		// p.add(labelclassification, new GridBagConstraints(0, 2, 1, 1, 0.0,
		// 0.0,
		// GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
		// 0, 0, 5, 5), 0, 0));
		//
		// p.add(registerset, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
		// GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
		// 0, 0, 5, 5), 0, 0));

		setLayout(new BorderLayout());
		add(p, BorderLayout.CENTER);
	}

	private void refreshJeuRegistre(PipeStopGroupList list) {

		if (list != null) {
			registerset.removeAllItems();
			for (PipeStopGroup r : list) {
				registerset.addItem(r.getName());
			}

		}

	}

	@Override
	public void informRegisterSetComponent(
			InstrumentPipeStopDescriptionComponent registercomponent) {
		super.informRegisterSetComponent(registercomponent);

		registercomponent
				.addRegisterSetListChangeListener(new RegisterSetListChangeListener() {

					public void registerSetListChanged(PipeStopGroupList newlist) {
						refreshJeuRegistre(newlist);
					}
				});
	}

	@Override
	public void load(AbstractTrackDef td) {

		try {
			logger.debug("load " + td); //$NON-NLS-1$

			NoteDef nd = (NoteDef) td;

			int midicode = nd.getMidiNote();
			spinneroctave.setValue(new Integer(MidiHelper.getOctave(midicode)));

			int midinote = MidiHelper.extractNoteFromMidiCode(midicode);

			LocalizedNoteChoose lnc = null;
			// recherche de l'élément associé à la note midi ....
			ComboBoxModel defaultmodel = note.getModel();
			for (int i = 0; i < defaultmodel.getSize(); i++) {
				LocalizedNoteChoose current = (LocalizedNoteChoose) defaultmodel
						.getElementAt(i);
				if (current.getNoteCode() == midinote) {
					lnc = current;
					break;
				}
			}

			if (lnc == null)
				throw new Exception("note not found"); //$NON-NLS-1$

			note.setSelectedItem(lnc);

			registerset.setSelectedItem(nd.getRegisterSetName());

		} catch (Exception ex) {
			logger.error("load", ex); //$NON-NLS-1$
		}
	}

	private void sendNewNote() {

		int midiNote = ((LocalizedNoteChoose) note.getSelectedItem())
				.getNoteCode();
		int octave = (Integer) spinneroctave.getValue();

		int midicode = MidiHelper.computeMidiCodeFromNoteAndOctave(midiNote,
				octave);

		String jeu = (String) registerset.getSelectedItem();

		fireTrackDefChanged(new NoteDef(midicode, jeu));

	}

	@SuppressWarnings("unchecked")//$NON-NLS-1$
	@Override
	public Class getEditedTrackDef() {
		return NoteDef.class;
	}

	@Override
	public String getTitle() {
		return Messages.getString("NoteDefComponent.4"); //$NON-NLS-1$
	}

	@Override
	public void sendTrackDef() {
		sendNewNote();
	}

}
