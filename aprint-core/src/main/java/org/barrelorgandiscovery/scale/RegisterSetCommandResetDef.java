package org.barrelorgandiscovery.scale;

import org.barrelorgandiscovery.tools.HashCodeUtils;

public class RegisterSetCommandResetDef extends AbstractRegisterCommandDef {

	/**
	 * 
	 */
	private static final long serialVersionUID = 592740250280909072L;

	private String registerset; // Tous ou un jeu de registre donné

	/**
	 * Constructeur
	 * 
	 * @param registerset
	 *            jeu de registre arreté ou ALL pour tous ...
	 * @param longueur
	 *            longueur de la commande, si celle ci est fixe (ou Double.NaN
	 *            sinon)
	 * @param retard
	 *            retard de la commande (si celle ci doit être en avance de
	 *            phase)
	 */
	public RegisterSetCommandResetDef(String registerset, double longueur,
			double retard) {
		super(longueur, retard);

		this.registerset = registerset;

	}

	/**
	 * Récupère le set de registre resetté
	 * 
	 * @return
	 */
	public String getRegisterSet() {
		return this.registerset;
	}

	@Override
	public int hashCode() {
		int l = HashCodeUtils.SEED;
		l = HashCodeUtils.hash(l, registerset);
		l = HashCodeUtils.hash(l, longueur);
		l = HashCodeUtils.hash(l, retard);
		return l;
	}

}
