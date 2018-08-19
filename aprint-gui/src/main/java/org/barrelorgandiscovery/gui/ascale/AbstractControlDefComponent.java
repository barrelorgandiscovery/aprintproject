package org.barrelorgandiscovery.gui.ascale;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.ControlTrackDef;


public abstract class AbstractControlDefComponent extends
		AbstractTrackDefComponent {

	/**
	 * Loggeur
	 */
	private static final Logger logger = Logger
			.getLogger(AbstractControlDefComponent.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -8811002736262470180L;

	protected JCheckBox chbxFixedLength;
	protected JSpinner spinnerFixedLength;

	protected JCheckBox chbxretard;
	protected JSpinner spinnerRetard;

	public AbstractControlDefComponent() {
		initComponent();
		layoutComponent();
	}

	/**
	 * Appelée par la classe AbstractControlDefComponent pour initialiser les
	 * composants du panneau cette méthode peut être surchargée par les classes
	 * dérivées
	 */
	protected void initComponent() {

		logger.debug("initComponent"); //$NON-NLS-1$

		chbxFixedLength = new JCheckBox(Messages.getString("AbstractControlDefComponent.1"), true); //$NON-NLS-1$
		chbxFixedLength.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				spinnerFixedLength.setEnabled(chbxFixedLength.isSelected());
				sendControlDef();
			}
		});
		
		spinnerFixedLength = new JSpinner(new SpinnerNumberModel(4.0, 0.0,
				100.0, 0.5));
		spinnerFixedLength.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				sendControlDef();
			}
		});

		chbxretard = new JCheckBox(Messages.getString("AbstractControlDefComponent.2"), true); //$NON-NLS-1$
		chbxretard.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				spinnerRetard.setEnabled(chbxretard.isSelected());
				sendControlDef();
			}
		});
		

		spinnerRetard = new JSpinner(new SpinnerNumberModel(4.0, 0.0, 100.0,
				0.5));
		spinnerRetard.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				sendControlDef();
			}
		});

	}

	/**
	 * Met en page les composants
	 */
	protected abstract void layoutComponent();

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.agamme.AbstractTrackDefComponent#load(fr.freydierepatrice.gamme.AbstractTrackDef)
	 */
	@Override
	public void load(AbstractTrackDef td) {

		ControlTrackDef cd = (ControlTrackDef) td;

		spinnerRetard.setEnabled(true);
		if (Double.isNaN(cd.getRetard())) {
			chbxretard.setSelected(false);
			spinnerRetard.setEnabled(false);
		} else {
			chbxretard.setSelected(true);
			spinnerRetard.setValue(new Double(cd.getRetard()));
		}

		spinnerFixedLength.setEnabled(true);
		if (Double.isNaN(cd.getLength())) {
			chbxFixedLength.setSelected(false);
			spinnerFixedLength.setEnabled(false);
		} else {
			chbxFixedLength.setSelected(true);
			spinnerFixedLength.setValue(new Double(cd.getLength()));
		}
	}

	/**
	 * Méthode interne pour envoyer les changements sur l'élément ...
	 */
	protected abstract void sendControlDef();

}
