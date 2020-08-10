package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * punch plan, containing the commands, and associated parameters for the optimizer
 * 
 * @author pfreydiere
 * 
 */
public class PunchPlan {

	/**
	 * list of the commands of the punch plan
	 */
	private ArrayList<Command> commands = new ArrayList<>();

	public PunchPlan() {
	}

	/**
	 * copy constructor
	 * 
	 * @param copy
	 */
	public PunchPlan(PunchPlan copy) {
		assert copy != null;
		commands.addAll(copy.commands);
		optimizerClass = copy.optimizerClass;
		optimizerParameters = copy.optimizerParameters;
	}

	/**
	 * class that implements optimizer for punch
	 */
	private Class<?> optimizerClass;

	public void setOptimizerClass(Class<?> optimizerClass) {
		this.optimizerClass = optimizerClass;
	}

	public Class<?> getOptimizerClass() {
		return optimizerClass;
	}

	private Map<String, Object> optimizerParameters;

	public void setOptimizerParameters(Map<String, Object> parameters) {
		this.optimizerParameters = parameters;
	}

	public Map<String, Object> getOptimizerParmeters() {
		return this.optimizerParameters;
	}

	public List<Command> getCommandsByRef() {
		return commands;
	}

	public XYCommand getXYCommandAtIndex(int index) {
		Command command = commands.get(index);
		if (command != null && command instanceof XYCommand) {
			return (XYCommand) command;
		}
		return null;
	}

	public XYCommand getLatestXYCommand() {
		int i = commands.size() - 1;
		while (i >= 0) {
			Command c = commands.get(i);
			if (c instanceof XYCommand) {
				return (XYCommand)c;
			}
			i--;
		}
		return null;
	}
	
	public XYCommand getFirstXYCommand() {
		int i = 0;
		while (i <= commands.size() - 1) {
			Command c = commands.get(i);
			if (c instanceof XYCommand) {
				return (XYCommand)c;
			}
			i++;
		}
		return null;
	}


}
