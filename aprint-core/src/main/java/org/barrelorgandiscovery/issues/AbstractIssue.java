package org.barrelorgandiscovery.issues;

public abstract class AbstractIssue {

	/**
	 * Libelle de l'erreur
	 */
	private int issuetype;

	public AbstractIssue(int issuetype) {
		this.issuetype = issuetype;
	}

	/**
	 * R�cup�ration du libelle de l'erreur
	 * 
	 * @return
	 */
	public int getType() {
		return this.issuetype;
	}

	/**
	 * Red�finition du libelle du probl�me
	 * 
	 * @param newlibelle
	 */
	protected void setType(int issuetype) {
		this.issuetype = issuetype;
	}

	/**
	 * Issue Label
	 * @return
	 */
	public abstract String toLabel();

}
