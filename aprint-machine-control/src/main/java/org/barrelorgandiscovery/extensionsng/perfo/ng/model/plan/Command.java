package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

/**
 * commands coordinates are in screen cartesian, from top left
 * @author use
 *
 */
public abstract class Command {

	public abstract void accept(int index,CommandVisitor visitor) throws Exception;

}
