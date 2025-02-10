package org.barrelorgandiscovery.gui.aprintng;

import org.barrelorgandiscovery.gui.tools.APrintStatusBarHandling;

/**
 * Interface handling statusbar
 * 
 * @author pfreydiere
 *
 */
public interface IStatusBarFeedBackTransactional extends IStatusBarFeedback {

	public APrintStatusBarHandling.StatusBarTransaction startTransaction();

	public void transactionProgress(APrintStatusBarHandling.StatusBarTransaction transaction, double progress);

	public void transactionText(APrintStatusBarHandling.StatusBarTransaction transaction, String text);

	public void endTransaction(APrintStatusBarHandling.StatusBarTransaction transaction);
}
