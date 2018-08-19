package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;

public interface PositionPanelListener {

	public void next();
	public void previous();
	public void first();
	public void pausePlay() throws Exception;
	
}
