package org.barrelorgandiscovery.scale;

public class ReferencedRegisterSet {

	
	private String name;
	
	private String libelle;
	
	public ReferencedRegisterSet(String name, String libelle)
	{
		this.name = name;
		this.libelle = libelle;
	}

	public String getName() {
		return name;
	}

	public String getLibelle() {
		return libelle;
	}
	
}
