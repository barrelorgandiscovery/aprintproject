package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import java.util.List;

public abstract class CommandVisitor {

	public CommandVisitor() {

	}

	public void visit(PunchPlan punchPlan) throws Exception {
		List<Command> commands = punchPlan.getCommandsByRef();
		
		for (int i = 0 ; i < commands.size(); i++ ) {
			Command c = commands.get(i);
			c.accept(i, this);
		}
	}
	
	public void visit(PunchPlan punchPlan, int fromIndex) throws Exception {
		List<Command> commands = punchPlan.getCommandsByRef();
		
		for (int i = fromIndex ; i < commands.size(); i++ ) {
			Command c = commands.get(i);
			c.accept(i, this);
		}
	}

	public abstract void visit(int index, PunchCommand punchCommand) throws Exception;

	public abstract void visit(int index, DisplacementCommand displacementCommand)
			throws Exception;
	
	public abstract void visit(int index, CutToCommand cutToCommand) throws Exception;

	public abstract void visit(int index, HomingCommand command) throws Exception;

}
