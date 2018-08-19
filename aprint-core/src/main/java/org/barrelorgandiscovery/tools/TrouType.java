package org.barrelorgandiscovery.tools;

public enum TrouType {

	TROUS_RECTANGULAIRES(0), TROUS_ARRONDIS(1), TROUS_RONDS(2);

	private int type;

	TrouType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}

}
