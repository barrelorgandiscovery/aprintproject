package org.barrelorgandiscovery.virtualbook.sigs;

/**
 * Class Handling a signature in the virtual book object
 * @author pfreydiere
 *
 */
public class ComputedSig {
	public long timeStamp;
	public int sigNumber;
	public long measureLength;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ComputedSig ").append(timeStamp).append(" # :")
				.append(sigNumber).append(" , measureLength :")
				.append(measureLength);
		return sb.toString();
	}

}
