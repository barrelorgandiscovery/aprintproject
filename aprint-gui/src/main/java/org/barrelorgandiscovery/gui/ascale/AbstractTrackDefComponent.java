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
	 * permet aux classes d�riv�es de d'envoyer un �venement de changement de
	 * d�finition de piste (pour le composant de gamme)
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
	 * Cette m�thode est appel�e pour informer le composant du composant de
	 * d�finition des registres
	 * 
	 * Les composants client peuvent s'enregistrer pour �tre au courant des
	 * modifications
	 * 
	 * @param registercomponent
	 */
	public void informRegisterSetComponent(
			InstrumentPipeStopDescriptionComponent registercomponent) {

	}

	/**
	 * R�cup�re la classe de l'objet edit� ..
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public abstract Class getEditedTrackDef();

	/**
	 * R�cup�re le titre du composant
	 * 
	 * @return
	 */
	public abstract String getTitle();

	/**
	 * Demande l'�mission d'un message de modification de l'�l�ment
	 */
	public abstract void sendTrackDef();

}
