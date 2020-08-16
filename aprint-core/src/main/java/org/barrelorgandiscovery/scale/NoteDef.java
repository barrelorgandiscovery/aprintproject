package org.barrelorgandiscovery.scale;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.HashCodeUtils;

public class NoteDef extends AbstractTrackDef {

	/**
	 * pour la sérialisation
	 */
	private static final long serialVersionUID = 881283155174363392L;

	/**
	 * note midi
	 */
	private int midinote;

	/**
	 * classification de la note (pour l'instant en chaine de caractère)
	 */
	private String registersetname; // chant, acompagnement, basse, tremolo ...

	/**
	 * Constructeur
	 * 
	 * @param midinote
	 *            définition midi de la note jouée
	 */
	public NoteDef(int midinote) {
		this.midinote = midinote;
	}

	/**
	 * Constructeur
	 * 
	 * @param midinote
	 *            définition midi de la note jouée
	 * @param registersetname
	 *            (catégorie de classification de la note)
	 */
	public NoteDef(int midinote, String registersetname) {
		this(midinote);
		this.registersetname = registersetname;
	}

	/**
	 * Récupère la définition midi de la note
	 * 
	 * @return
	 */
	public int getMidiNote() {
		return midinote;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || obj.getClass() != getClass())
			return false;

		NoteDef d = (NoteDef) obj;

		if (registersetname != null) {

			if (!registersetname.equals(d.registersetname))
				return false;
			
		} else {
			// classification est null
			if (d.registersetname != null)
				return false;
			
			// invariant, les deux sont null
		}

		return d.midinote == midinote;
	}

	@Override
	public String toString() {
		return Messages.getString("NoteDef.0") + midinote + " - " + registersetname; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Retourne la classification de la note ...
	 * 
	 * @return
	 */
	public String getRegisterSetName() {
		return this.registersetname;
	}
	
	@Override
	public int hashCode() {
		int l = HashCodeUtils.SEED;
		l = HashCodeUtils.hash(l, midinote);
		l = HashCodeUtils.hash(l, registersetname);
		return l;
	}

}
