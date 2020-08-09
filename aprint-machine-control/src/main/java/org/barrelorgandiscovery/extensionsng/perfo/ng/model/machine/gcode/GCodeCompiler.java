package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode;

import java.util.List;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CommandVisitor;

public abstract class GCodeCompiler extends CommandVisitor {

	public abstract void reset();

	public abstract List<String> getGCODECommands();

}
