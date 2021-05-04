package org.barrelorgandiscovery.gui.aprintng;

import org.barrelorgandiscovery.gui.ICancelTracker;

/**
 * interface implemented by visual component, permitting to
 * show an async process in progress
 * @author pfreydiere
 *
 */
public interface IAPrintWait {

	/**
	 * Start the wait process
	 * @param text
	 * @param cancelTracker
	 */
	public abstract void infiniteStartWait(String text,
			ICancelTracker cancelTracker);

	/**
	 * Fonction de lancement de la procedure d'attente d'APrint, doit être lancé
	 * dans le thread de swing
	 */
	public abstract void infiniteStartWait(String text);

	/**
	 * Fonction de lancement de la procedure d'attente d'APrint, doit être lancé
	 * dans le thread de swing
	 */
	public abstract void infiniteEndWait();

	/**
	 * change wait text
	 * @param text the new text
	 */
	public abstract void infiniteChangeText(final String text);

}