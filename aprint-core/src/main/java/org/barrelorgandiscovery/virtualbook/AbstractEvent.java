package org.barrelorgandiscovery.virtualbook;

import java.io.Serializable;

/**
 * Class d'�v�nement permettant la gestion d'�v�nements supl�mentaires associ�s
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
	 * R�cup�re l'horodatage de l'�v�nement
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
	 * D�finition de l'ordre de trie
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
