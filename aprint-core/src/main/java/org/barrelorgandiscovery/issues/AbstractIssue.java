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
	 * Récupération du libelle de l'erreur
	 * 
	 * @return
	 */
	public int getType() {
		return this.issuetype;
	}

	/**
	 * Redéfinition du libelle du problème
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
