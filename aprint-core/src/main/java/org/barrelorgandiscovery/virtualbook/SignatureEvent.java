package org.barrelorgandiscovery.virtualbook;

/**
 * a signature change event
 * 
 * @author Freydiere Patrice
 * 
 */
public class SignatureEvent extends AbstractEvent {

	private final int numerateur;
	private final int denominateur;

	public SignatureEvent(long timestamp, int numerateur, int denominateur) {
		super(timestamp);
		this.numerateur = numerateur;
		this.denominateur = denominateur;
	}

	public int getNumerateur() {
		return this.numerateur;
	}

	public int getDenominateur() {
		return this.denominateur;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Signature Event :").append(this.getTimestamp()).append(" ")
				.append(numerateur).append("/").append(denominateur);
		return sb.toString();
	}
}
