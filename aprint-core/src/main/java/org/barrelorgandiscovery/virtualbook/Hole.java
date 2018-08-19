package org.barrelorgandiscovery.virtualbook;

import java.io.Serializable;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.timed.ITimedLength;
import org.barrelorgandiscovery.timed.ITimedStamped;
import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * hole representation in the virtual book
 * 
 * @author Freydiere Patrice
 */
public class Hole implements Comparable<Hole>, Serializable, ITimedStamped, ITimedLength {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1683203299759754423L;

	/**
	 * Temps en microseconde par rapport au debut du carton
	 */
	private long timestamp;

	/**
	 * Longueur de la note en microseconde par rapport au debut de la note
	 */
	private long length;

	/**
	 * piste
	 */
	private int piste;

	/**
	 * Constructor
	 * 
	 * @param piste
	 *            hole track
	 * @param timestamp
	 *            timestamp in microsecond
	 * @param length
	 *            length of the hole in microsecond
	 */
	public Hole(int piste, long timestamp, long length) {
		super();
		this.piste = piste;
		this.timestamp = timestamp;
		// assert length >= 0;
		this.length = length;
	}

	/**
	 * Constructeur par copie
	 * 
	 * @param n
	 *            la note à copier
	 */
	public Hole(Hole n) {
		super();
		this.piste = n.piste;
		this.timestamp = n.timestamp;
		this.length = n.length;
	}

	/**
	 * length of the hole in microseconds
	 * 
	 * @return
	 */
	public long getTimeLength() {
		return length;
	}

	/**
	 * Get the track of the hole
	 * 
	 * @return
	 */
	public int getTrack() {
		return piste;
	}

	/**
	 * Time of the start of the Hole (in Microseconds since the beginning of the
	 * Book)
	 * 
	 * @return
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * function that return intersection with a region
	 * 
	 * @param r
	 * @return
	 */
	public boolean intersect(Region r) {
		if (piste < r.beginningtrack)
			return false;
		if (piste > r.endtrack)
			return false;

		if (timestamp > r.end)
			return false;
		if (timestamp + length < r.start)
			return false;

		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		Hole n = (Hole) obj;
		return timestamp == n.timestamp && piste == n.piste
				&& length == n.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = HashCodeUtils.SEED;
		hash = HashCodeUtils.hash(hash, timestamp);
		hash = HashCodeUtils.hash(hash, piste);
		hash = HashCodeUtils.hash(hash, length);
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" ").append(timestamp).append(" ")
				.append(Messages.getString("Hole.0")).append(" ").append(piste) //$NON-NLS-1$
				.append(" ").append(Messages.getString("Hole.1")).append(" ")
				.append(length)
				.append(" ").append(Messages.getString("Hole.2")); //$NON-NLS-1$ //$NON-NLS-2$
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	final public int compareTo(Hole o) {

		if (timestamp < o.timestamp)
			return -1;

		if (timestamp > o.timestamp)
			return 1;

		// Invariant : o1.getTimestamp = o2.getTimestamp

		if (piste < o.piste)
			return -1;

		if (piste > o.piste)
			return 1;

		if (length < o.length)
			return -1;

		if (length > o.length)
			return 1;

		return 0;
	}

	/**
	 * are two holes intersecting ?
	 * 
	 * @param a
	 *            la première note
	 * @param b
	 *            la seconde note
	 * @return
	 */
	public final static boolean isIntersect(Hole a, Hole b) {

		if (a == null || b == null) {
			return false;
		}

		if (a.getTrack() == b.getTrack()) {
			
			long ats = a.getTimestamp();
			long bts = b.getTimestamp();
			if (!(ats > bts + b.getTimeLength() || ats + a.getTimeLength() < bts)) {
				return true;
			}
			
		}
		return false;
	}

	/**
	 * get the intersection between two holes
	 * 
	 * @param a
	 *            first hole
	 * @param b
	 *            second hole
	 * @return
	 */
	public static Hole intersect(Hole a, Hole b) {
		if (a == null || b == null)
			return null;

		if (a.getTrack() != b.getTrack())
			return null;

		if (!isIntersect(a, b))
			return null;

		long start = Math.max(a.getTimestamp(), b.getTimestamp());

		long length = Math.min(a.getTimeLength() + a.getTimestamp(),
				b.getTimeLength() + b.getTimestamp())
				- start;

		Hole n = new Hole(a.getTrack(), start, length);

		return n;
	}

	/**
	 * merge two holes (union operator)
	 * 
	 * @param a
	 *            first hole
	 * @param b
	 *            second hole
	 * @return the union of the two holes
	 */
	public static Hole union(Hole a, Hole b) {
		if (a == null || b == null)
			return null;

		if (a.getTrack() != b.getTrack())
			return null;

		if (!isIntersect(a, b))
			return null;

		long start = Math.min(a.getTimestamp(), b.getTimestamp());

		long length = Math.max(a.getTimeLength() + a.getTimestamp(),
				b.getTimeLength() + b.getTimestamp())
				- start;

		Hole n = new Hole(a.getTrack(), start, length);

		return n;
	}

	/**
	 * Teste si la note a est complètement à l'intérieur de b
	 * 
	 * @param a
	 *            la première note
	 * @param b
	 *            la note de test
	 */
	public static boolean isIn(Hole a, Hole b) {

		if (a == null || b == null)
			return false;

		if (a.timestamp >= b.timestamp
				&& a.timestamp + a.length <= b.timestamp + b.length)
			return true;

		return false;

	}

	/**
	 * Construit l'expression a - b
	 * 
	 * @param a
	 *            la note de depart
	 * @param b
	 *            la note de soustraction
	 * @return
	 */
	public static Hole[] minus(Hole a, Hole b) {

		if (a == null)
			return null;

		if (b == null)
			return new Hole[] { new Hole(a) };

		if (isIntersect(a, b)) {

			if (isIn(a, b)) {
				// a est completement dans b
				return new Hole[0];
			} else if (isIn(b, a)) {
				// b est dans a

				// on coupe la note en deux
				return new Hole[] {
						new Hole(a.piste, a.timestamp, b.timestamp
								- a.timestamp),
						new Hole(a.piste, b.timestamp + b.length, a.timestamp
								+ a.length - (b.timestamp + b.length))

				};

			} else {

				// on rapetisse la note, il faut savoir dans quel sens on est

				if (a.timestamp < b.timestamp) {
					return new Hole[] { new Hole(a.piste, a.timestamp,
							b.timestamp - a.timestamp) };
				} else // a.getTimestamp() > b.getTimestamp()
				{
					return new Hole[] { new Hole(a.piste, b.timestamp
							+ b.length, a.timestamp + a.length
							- (b.timestamp + b.length)) };
				}

			}

		} else {
			// a et b ne s'intersecte pas, on retourne a
			return new Hole[] { new Hole(a) };
		}

	}

	/**
	 * Create a new hole and use an offset
	 * 
	 * @param timestamp
	 * @return
	 */
	public Hole newHoleWithOffset(long timestamp) {
		return new Hole(this.piste, this.timestamp + timestamp, this.length);
	}

	/**
	 * create a new hole with a minimum length
	 * @param minimumLength
	 * @return
	 */
	public Hole newHoleWithMinimumLength(long minimumLength) {
		return new Hole(this.piste, this.timestamp , Math.max(this.length, minimumLength));
	}
	
}
