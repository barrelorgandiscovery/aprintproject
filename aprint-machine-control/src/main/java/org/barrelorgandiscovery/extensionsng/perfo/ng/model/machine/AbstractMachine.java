package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;

/**
 * abstract machine command, interpreting the punchplan
 * 
 * @author pfreydiere
 * 
 */
public abstract class AbstractMachine {

	
	public AbstractMachine() {

	}

	/**
	 * get the label of the machine
	 * 
	 * @return
	 */
	public abstract String getTitle();

	/**
	 * get the description of the machine
	 * 
	 * @return
	 */
	public abstract String getDescription();


	/**
	 * open the machine control, 
	 * this raise exception if the machine is not ready or connected
	 * 
	 * @param punchPlan
	 */
	public abstract MachineControl open(AbstractMachineParameters parameters) throws Exception;

}
