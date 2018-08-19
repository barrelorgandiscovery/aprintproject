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

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.RegisterSetCommandResetDef;


/**
 * Panneau de saisie d'un arret d'un jeu de registre
 * 
 * @author Freydiere Patrice
 * 
 */
public class RegisterResetDefComponent extends AbstractControlDefComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8085569160759547540L;

	protected JLabel labelRegisterSet;
	protected JComboBox cbRegisterSet;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.agamme.AbstractControlDefComponent#initComponent()
	 */
	@Override
	protected void initComponent() {
		super.initComponent();

		labelRegisterSet = new JLabel(Messages
				.getString("RegisterResetDefComponent.0")); //$NON-NLS-1$

		cbRegisterSet = new JComboBox();
		cbRegisterSet.addItemListener(new ItemListener() {
			@SuppressWarnings(value="unused")
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String registersetname = (String) e.getItem();
					sendControlDef();
				}
			}
		});

		cbRegisterSet.addItem("ALL"); // tous les jeux sont arretés //$NON-NLS-1$
										// //$NON-NLS-1$

		cbRegisterSet.setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				if (value == null)
					return new JLabel();

				return new JLabel(Messages.getString("Register." //$NON-NLS-1$
						+ (String) value));
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.agamme.AbstractControlDefComponent#layoutComponent()
	 */
	@Override
	protected void layoutComponent() {

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());

		p.add(labelRegisterSet, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 5), 0, 0));
		p.add(cbRegisterSet, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.agamme.AbstractControlDefComponent#load(fr.freydierepatrice.gamme.AbstractTrackDef)
	 */
	@Override
	public void load(AbstractTrackDef td) {
		super.load(td);

		RegisterSetCommandResetDef nd = (RegisterSetCommandResetDef) td;

		cbRegisterSet.setSelectedItem(nd.getRegisterSet());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.agamme.AbstractControlDefComponent#sendControlDef()
	 */
	@Override
	protected void sendControlDef() {

		double longueur = Double.NaN;
		double retard = Double.NaN;

		if (chbxFixedLength.isSelected()) {
			longueur = ((Number) spinnerFixedLength.getValue()).doubleValue();
		}

		if (chbxretard.isSelected()) {
			longueur = ((Number) spinnerRetard.getValue()).doubleValue();
		}

		fireTrackDefChanged(new RegisterSetCommandResetDef(
				(String) cbRegisterSet.getSelectedItem(), longueur, retard));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.agamme.AbstractTrackDefComponent#getEditedTrackDef()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class getEditedTrackDef() {
		return RegisterSetCommandResetDef.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.agamme.AbstractTrackDefComponent#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.getString("RegisterResetDefComponent.3"); //$NON-NLS-1$
	}

	@SuppressWarnings(value="unused")
	private PipeStopGroupList registersetlist;

	/**
	 * rafraichit le jeu de registre
	 * 
	 * @param list
	 */
	private void refreshJeuRegistre(PipeStopGroupList list) {

		registersetlist = list;
		// rafraichissement du combo

		cbRegisterSet.removeAllItems();

		cbRegisterSet.addItem("ALL"); // tous les jeux sont arretés //$NON-NLS-1$
										// //$NON-NLS-1$
		for (PipeStopGroup s : list) {
			cbRegisterSet.addItem(s.getName());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.agamme.AbstractTrackDefComponent#informRegisterSetComponent(fr.freydierepatrice.agamme.RegisterSetComponent)
	 */
	@Override
	public void informRegisterSetComponent(
			InstrumentPipeStopDescriptionComponent registercomponent) {
		super.informRegisterSetComponent(registercomponent);

		registercomponent
				.addRegisterSetListChangeListener(new RegisterSetListChangeListener() {

					public void registerSetListChanged(PipeStopGroupList newlist) {
						registersetlist = newlist;
						refreshJeuRegistre(newlist);
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.agamme.AbstractTrackDefComponent#sendTrackDef()
	 */
	@Override
	public void sendTrackDef() {
		sendControlDef();
	}

}
