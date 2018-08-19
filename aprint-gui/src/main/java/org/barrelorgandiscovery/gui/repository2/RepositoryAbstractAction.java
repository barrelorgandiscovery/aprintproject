package org.barrelorgandiscovery.gui.repository2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

public abstract class RepositoryAbstractAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7564509659292070843L;

	private static Logger logger = Logger
			.getLogger(RepositoryAbstractAction.class);

	protected CurrentRepositoryInformations infos;

	protected Object parent;

	public RepositoryAbstractAction(Object parent,
			CurrentRepositoryInformations infos) {
		this.infos = infos;
		this.parent = parent;
	}

	protected abstract void safeActionPerformed(ActionEvent e) throws Exception;


	public void actionPerformed(ActionEvent e) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("launch action " + getClass().getName());
			safeActionPerformed(e);
			if (logger.isDebugEnabled())
				logger.debug("action " + getClass().getName() + " done");

		} catch (Exception ex) {
			logger.error(
					"error in action " + getClass().getName() + " "
							+ ex.getMessage(), ex);
			BugReporter.sendBugReport();
			JMessageBox.showError(parent, ex);
		}
	}

}
