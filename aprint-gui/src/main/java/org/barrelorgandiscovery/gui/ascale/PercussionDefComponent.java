package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.ReferencedPercussion;
import org.barrelorgandiscovery.scale.ReferencedPercussionList;

public class PercussionDefComponent extends AbstractControlDefComponent {

	/**
	 * Loggeur
	 */
	private static final Logger logger = Logger
			.getLogger(PercussionDefComponent.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -8811002736262470180L;

	private JLabel labelPercussion;
	private JComboBox comboboxPercussion;

	public PercussionDefComponent() {
	}

	protected void initComponent() {
		logger.debug("initcomponent"); //$NON-NLS-1$
		super.initComponent();

		labelPercussion = new JLabel(
				Messages.getString("PercussionDefComponent.1")); //$NON-NLS-1$

		comboboxPercussion = new JComboBox(
				ReferencedPercussionList.getReferencedPercussion());
		comboboxPercussion.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					sendControlDef();
				}
			}
		});
		comboboxPercussion.setRenderer(new ListCellRenderer() {

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				ReferencedPercussion referencedPercussion = (ReferencedPercussion) value;

				return new JLabel(""
						+ referencedPercussion.getMidicode()
						+ " - "
						+ ReferencedPercussion
								.getLocalizedDrumLabel(referencedPercussion));

			}
		});

		// comboboxPercussion.addItem(null);

	}

	@Override
	protected void layoutComponent() {

		logger.debug("layoutComponent"); //$NON-NLS-1$

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());

		p.add(labelPercussion, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 5), 0, 0));
		p.add(comboboxPercussion, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 5), 0, 0));

		p.add(chbxFixedLength, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 5), 0, 0));
		p.add(spinnerFixedLength, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 5), 0, 0));

		p.add(chbxretard, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 5), 0, 0));

		p.add(spinnerRetard, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 5), 0, 0));

		setLayout(new BorderLayout());
		add(p, BorderLayout.CENTER);
	}

	@Override
	public void load(AbstractTrackDef td) {

		logger.debug("load " + td); //$NON-NLS-1$

		super.load(td);

		PercussionDef pd = (PercussionDef) td;

		int percussionMidiCode = pd.getPercussion();

		ReferencedPercussion referencedPercussionByMidiCode = ReferencedPercussionList
				.findReferencedPercussionByMidiCode(percussionMidiCode);
		logger.debug("referenced percussion found for code "
				+ percussionMidiCode + " :" + referencedPercussionByMidiCode);

		comboboxPercussion.setSelectedItem(referencedPercussionByMidiCode);

	}

	@Override
	protected void sendControlDef() {

		ReferencedPercussion p = (ReferencedPercussion) comboboxPercussion
				.getSelectedItem();

		int percussioncode = p.getMidicode();

		double longueur = Double.NaN;
		double retard = Double.NaN;

		if (chbxFixedLength.isSelected()) {
			longueur = ((Number) spinnerFixedLength.getValue()).doubleValue();
		}

		if (chbxretard.isSelected()) {
			retard = ((Number) spinnerRetard.getValue()).doubleValue();
		}

		fireTrackDefChanged(new PercussionDef(percussioncode, retard, longueur));

	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getEditedTrackDef() {
		return PercussionDef.class;
	}

	@Override
	public String getTitle() {
		return Messages.getString("PercussionDefComponent.4"); //$NON-NLS-1$
	}

	@Override
	public void sendTrackDef() {
		sendControlDef();
	}
}
