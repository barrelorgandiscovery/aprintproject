package org.barrelorgandiscovery.virtualbook;

import java.io.Serializable;

/**
 * Class d'évènement permettant la gestion d'évènements suplémentaires associés
 * au carton
 * 
 * @author Freydiere Patrice
 * 
 */
public abstract class AbstractEvent implements Comparable<AbstractEvent>,
		Serializable {

	private long timestamp;

	public AbstractEvent(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Récupère l'horodatage de l'évènement
	 * 
	 * @return
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * shift the event of a specific time
	 * 
	 * @param time
	 */
	public void shift(long time) {
		this.timestamp += time;
	}

	/**
	 * Définition de l'ordre de trie
	 */
	public int compareTo(AbstractEvent o) {

		assert o != null;

		if (timestamp < o.timestamp)
			return -1;

		if (timestamp > o.timestamp)
			return 1;

		assert timestamp == o.timestamp;

		return ((Integer) ((Long) timestamp).hashCode())
				.compareTo(((Long) o.timestamp).hashCode());
	}

}
