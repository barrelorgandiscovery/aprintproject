package org.barrelorgandiscovery.virtualbook.checker;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.virtualbook.VirtualBook;


/**
 * a checker that implement a composite check
 */
public class CompositeChecker implements Checker {

	private static final Logger logger = Logger
			.getLogger(CompositeChecker.class);

	private Checker[] checkers;

	/**
	 * Constructor, take in parameters the array of check to launch in a check
	 * 
	 * @param checkers
	 */
	public CompositeChecker(Checker[] checkers) {
		this.checkers = checkers;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.freydierepatrice.virtualbook.checker.Checker#check(fr.freydierepatrice.virtualbook.VirtualBook)
	 */
	public IssueCollection check(VirtualBook carton) throws Exception {

		logger.debug("check"); //$NON-NLS-1$

		IssueCollection ic = new IssueCollection();

		for (int i = 0; i < checkers.length; i++) {

			Checker current = checkers[i];
			logger.debug("launch checker " + current); //$NON-NLS-1$

			if (current == null)
				continue;

			IssueCollection tmp = current.check(carton);
			if (tmp != null) {
				logger.debug("" + tmp.size() + " elements found"); //$NON-NLS-1$ //$NON-NLS-2$
				ic.addAll(tmp);
			} else {
				logger.debug("checker " + current + " return null"); //$NON-NLS-1$ //$NON-NLS-2$
			}

		}

		return ic;
	}
}
