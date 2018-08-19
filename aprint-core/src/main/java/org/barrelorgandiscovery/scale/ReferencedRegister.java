package org.barrelorgandiscovery.scale;

public class ReferencedRegister {

	
	private String name;
	
	private String libelle;
	
	public ReferencedRegister(String name, String libelle)
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
