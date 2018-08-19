package org.barrelorgandiscovery.gui.ascale;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractTrackDef;


public abstract class AbstractTrackDefComponent extends JComponent {

	private static final Logger logger = Logger
			.getLogger(AbstractTrackDefComponent.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -7409868956803458620L;

	public abstract void load(AbstractTrackDef td);

	private TrackDefComponentListener listener = null;

	public void setTrackDefComponentListener(TrackDefComponentListener tdl) {
		this.listener = tdl;
	}

	/**
	 * permet aux classes dérivées de d'envoyer un évenement de changement de
	 * définition de piste (pour le composant de gamme)
	 * 
	 * @param td
	 */
	protected void fireTrackDefChanged(AbstractTrackDef td) {
		logger.debug("fireTrackDefChanged " + td); //$NON-NLS-1$

		if (listener == null)
			return;

		listener.trackDefChanged(td);
	}

	/**
	 * Cette méthode est appelée pour informer le composant du composant de
	 * définition des registres
	 * 
	 * Les composants client peuvent s'enregistrer pour être au courant des
	 * modifications
	 * 
	 * @param registercomponent
	 */
	public void informRegisterSetComponent(
			InstrumentPipeStopDescriptionComponent registercomponent) {

	}

	/**
	 * Récupère la classe de l'objet edité ..
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public abstract Class getEditedTrackDef();

	/**
	 * Récupère le titre du composant
	 * 
	 * @return
	 */
	public abstract String getTitle();

	/**
	 * Demande l'émission d'un message de modification de l'élément
	 */
	public abstract void sendTrackDef();

}
