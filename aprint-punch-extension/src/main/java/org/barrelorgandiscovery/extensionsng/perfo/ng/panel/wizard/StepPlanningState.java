package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.io.Serializable;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;

public class StepPlanningState implements Serializable {

	public PunchPlan punchPlan;
	public AbstractMachine machine;
	public Serializable parameters;
	public Class optimizerClass;
	
}
