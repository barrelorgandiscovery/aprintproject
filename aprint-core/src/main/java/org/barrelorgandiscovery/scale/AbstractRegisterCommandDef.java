package org.barrelorgandiscovery.scale;

/**
 * D�finition d'une piste encodant une commande de registre
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
