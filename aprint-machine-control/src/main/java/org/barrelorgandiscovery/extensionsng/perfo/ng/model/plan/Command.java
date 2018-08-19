package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

public abstract class Command {

	public abstract void accept(int index,CommandVisitor visitor) throws Exception;

}
