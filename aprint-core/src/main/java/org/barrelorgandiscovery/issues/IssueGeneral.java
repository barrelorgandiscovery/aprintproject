package org.barrelorgandiscovery.issues;

import org.barrelorgandiscovery.messages.Messages;

public class IssueGeneral extends AbstractIssue {

	private String GENERIC_DESCRIPTION_KEY = null;

	public IssueGeneral(int type, String GENERIC_DESCRIPTION_KEY) {
		super(type);
		this.GENERIC_DESCRIPTION_KEY = GENERIC_DESCRIPTION_KEY;
	}

	@Override
	public String toLabel() {
		return "General :"
				+ getType()
				+ (GENERIC_DESCRIPTION_KEY != null ? " - " //$NON-NLS-1$
						+ Messages.getString("ISSUES." //$NON-NLS-1$
								+ GENERIC_DESCRIPTION_KEY) : ""); //$NON-NLS-1$
	}

}
