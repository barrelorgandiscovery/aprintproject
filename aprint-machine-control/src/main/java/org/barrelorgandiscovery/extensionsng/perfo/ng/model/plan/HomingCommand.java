package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

public class HomingCommand extends Command {

	public HomingCommand() {

	}

	@Override
	public void accept(int index,CommandVisitor visitor) throws Exception {
		visitor.visit(index, this);
	}
	
	@Override
	public String toString() {
		return "Homing";
	}

}
