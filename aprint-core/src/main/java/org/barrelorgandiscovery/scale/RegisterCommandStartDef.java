package org.barrelorgandiscovery.scale;

import org.barrelorgandiscovery.tools.HashCodeUtils;

public class RegisterCommandStartDef extends AbstractRegisterCommandDef {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4689010420165268507L;

	/**
	 * Définition du jeu de registre associé
	 */
	private String jeuregistre;

	/**
	 * Définition du registre sélectionné dans le jeu
	 */
	private String registre;

	public RegisterCommandStartDef(String jeuregistre, String registre,
			double longueur, double retard) {
		super(longueur, retard);

		this.jeuregistre = jeuregistre;
		this.registre = registre;
	}

	public String getRegisterSetName() {
		return jeuregistre;
	}

	public String getRegisterInRegisterSet() {
		return registre;
	}

	@Override
	public int hashCode() {

		int l = HashCodeUtils.SEED;
		l = HashCodeUtils.hash(l, jeuregistre);
		l = HashCodeUtils.hash(l, registre);
		l = HashCodeUtils.hash(l, longueur);
		l = HashCodeUtils.hash(l, retard);

		return l;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || obj.getClass() != getClass())
			return false;

		RegisterCommandStartDef d = (RegisterCommandStartDef) obj;

		if (jeuregistre != null) {

			if (!jeuregistre.equals(d.jeuregistre))
				return false;

		} else {
			// classification est null
			if (d.jeuregistre != null)
				return false;

			// invariant, les deux sont null
		}

		if (registre != null) {

			if (!registre.equals(d.registre))
				return false;

		} else {
			// classification est null
			if (d.registre != null)
				return false;

			// invariant, les deux sont null
		}

		return Double.doubleToLongBits(d.retard) == Double
				.doubleToLongBits(retard)
				&& Double.doubleToLongBits(d.longueur) == Double
						.doubleToLongBits(longueur);
	}

}
