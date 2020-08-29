package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.io.Serializable;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;

public class StepPlanningState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8135458331502058710L;
	
	public PunchPlan punchPlan;
	public AbstractMachine machine;
	public Serializable parameters;
	public Class optimizerClass;
	
	@Override
	public String toString() {
		return "[Punch plan :" + punchPlan + ", machine :" + machine + " , parameters :" + parameters +", optimizer :" + optimizerClass;
	}
	
}
