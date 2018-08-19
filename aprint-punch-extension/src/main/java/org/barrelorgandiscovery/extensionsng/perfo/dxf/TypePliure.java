package org.barrelorgandiscovery.extensionsng.perfo.dxf;

public enum TypePliure {

	CONTINUE(0), POINTILLEE(1);

	private int type;

	TypePliure(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
}
