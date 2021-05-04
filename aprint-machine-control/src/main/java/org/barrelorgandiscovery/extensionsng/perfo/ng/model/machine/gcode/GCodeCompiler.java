package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode;

import java.util.List;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CommandVisitor;

/**
 * Compiler for GCODE, all commands contains LF ending characters 
 * 
 * @author pfreydiere
 */
public abstract class GCodeCompiler extends CommandVisitor {

	/**
	 * reset visitor state
	 */
	public abstract void reset();

	
	/**
	 * commands at the beginning of the generation, or commands, 
	 * used for example to start handling machine command
	 * @return
	 */
	public abstract List<String> getPreludeCommands();
	
	/**
	 * get all GCODE commands
	 * @return
	 */
	public abstract List<String> getGCODECommands();
	
	/**
	 * commands for stopping machine
	 * @return
	 */
	public abstract List<String> getEndingCommands();

}
