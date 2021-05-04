package org.barrelorgandiscovery.scale;

/**
 * Définition d'une piste encodant une commande de registre
 * 
 * @author Freydiere Patrice
 * 
 */
public abstract class AbstractRegisterCommandDef extends ControlTrackDef {

	/**
	 * Constructeur
	 * 
	 * @param jeuderegistre
	 */
	public AbstractRegisterCommandDef(double longueur, double retard) {
		super(longueur, retard);
	}


}
