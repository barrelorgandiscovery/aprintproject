package org.barrelorgandiscovery.scale;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.HashCodeUtils;

public class PercussionDef extends ControlTrackDef implements
		Comparable<PercussionDef> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1359186127570460331L;

	/**
	 * identifiant de la percussion
	 */
	private int percussion;

	/**
	 * Constructeur
	 * 
	 * @param percussionMidiNote
	 *            identifiant midi de la percussion
	 * @param retard
	 *            retard de l'instrument par rapport au jeu (en mm sur le
	 *            carton)
	 * @param longueur
	 *            longueur standardisée de la commande de la percussion (en mm
	 *            sur le carton)
	 */
	public PercussionDef(int percussionMidiNote, double retard, double longueur) {
		super(longueur, retard);
		this.percussion = percussionMidiNote;

	}

	/**
	 * Récupère la définition midi de la percussion
	 * 
	 * @return
	 */
	public int getPercussion() {
		return percussion;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (obj.getClass() == getClass()) {
			PercussionDef d = (PercussionDef) obj;
			return d.percussion == percussion
					&& Double.doubleToLongBits(d.retard) == Double
							.doubleToLongBits(retard)
					&& Double.doubleToLongBits(d.longueur) == Double
							.doubleToLongBits(longueur);
			// note PF : un NaN != NaN
		}

		return false;
	}
	
	@Override
	public int hashCode() {
		int l = HashCodeUtils.SEED;
		l = HashCodeUtils.hash(l, percussion);
		l = HashCodeUtils.hash(l, longueur);
		l = HashCodeUtils.hash(l, retard);
		return l;
	}

	@Override
	public String toString() {
		return Messages.getString("PercussionDef.0") + percussion + Messages.getString("PercussionDef.1") + longueur //$NON-NLS-1$ //$NON-NLS-2$
				+ Messages.getString("PercussionDef.2") + retard; //$NON-NLS-1$
	}

	public int compareTo(PercussionDef o) {

		int compare = 0;
		if (percussion > o.percussion) {
			compare = 1;
		} else if (percussion < o.percussion) {
			compare = -1;
		}

		if (compare == 0) {
			compare = Double.compare(retard, o.retard);
		}

		if (compare == 0) {
			compare = Double.compare(longueur, o.longueur);
		}

		return compare;
	}

}
