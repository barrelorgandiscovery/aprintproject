package org.barrelorgandiscovery.scale;

public abstract class ControlTrackDef extends AbstractTrackDef {

	
	public ControlTrackDef(double longueur, double retard) {
		this.longueur = longueur;
		this.retard = retard;
	}

	/**
	 * longueur pour la percussion (en mm) (taille normalisée) par défaut 6mm
	 * pour la grosse caisse, ou 5 mm sinon (caisse clair)
	 */
	protected double longueur;
	/**
	 * retard de l'instrument par rapport au jeu (en mm)
	 */
	protected double retard;

	/**
	 * retourne la longueur standard de la percussion sur le carton (en mm)
	 * 
	 * @return
	 */
	public double getLength() {
		return this.longueur;
	}

	/**
	 * Retoune le retard à la percussion (en mm)
	 * 
	 * @return NaN si non défini
	 */
	public double getRetard() {
		return this.retard;
	}

}
