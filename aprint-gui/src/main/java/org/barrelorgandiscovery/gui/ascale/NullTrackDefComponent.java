package org.barrelorgandiscovery.gui.ascale;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;

public class NullTrackDefComponent extends AbstractTrackDefComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2703934437348078974L;

	@SuppressWarnings("unchecked")
	@Override
	public Class getEditedTrackDef() {
		return null;
	}

	@Override
	public String getTitle() {
		return Messages.getString("NullTrackDefComponent.0"); //$NON-NLS-1$
	}

	@Override
	public void load(AbstractTrackDef td) {

	}

	@Override
	public void sendTrackDef() {
		fireTrackDefChanged(null);
	}

}
