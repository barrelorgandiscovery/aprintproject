package org.barrelorgandiscovery.optimizers;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.optimizers.model.Punch;

public class PunchDefaultConverter {

	/**
	 * create a default punch plan
	 * 
	 * @param punches
	 * @return
	 */
	public static PunchPlan createDefaultPunchPlan(Punch[] punches) {
		PunchPlan pp = new PunchPlan();
		for (Punch p : punches) {
			if (p == null)
				continue;
			pp.getCommandsByRef().add(new PunchCommand(p.x, p.y));
		}
		return pp;
	}

}
