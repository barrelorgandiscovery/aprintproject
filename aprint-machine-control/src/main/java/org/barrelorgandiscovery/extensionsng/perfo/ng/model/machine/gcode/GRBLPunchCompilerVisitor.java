package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CutToCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;

/**
 * transform generic commands into GCode orders
 * 
 * @author pfreydiere
 * 
 */
public class GRBLPunchCompilerVisitor extends GCodeCompiler {

	protected ArrayList<String> grblCommands = new ArrayList<>();

	public GRBLPunchCompilerVisitor() {

	}

	@Override
	public void reset() {
		grblCommands = new ArrayList<>();
	}

	@Override
	public void visit(int index, PunchCommand punchCommand) throws Exception {
		///
		/// SEE that the coordinates X / Y are inverted
		///
		grblCommands.add(String.format(Locale.ENGLISH, "G90 X%1$f Y%2$f\n", //$NON-NLS-1$
				punchCommand.getY(), punchCommand.getX()));
		grblCommands.add("M100\n"); //$NON-NLS-1$
	}

	@Override
	public void visit(int index, DisplacementCommand displacementCommand) throws Exception {
		///
		/// SEE that the coordinates X / Y are inverted
		///

		grblCommands.add(String.format(Locale.ENGLISH, "G90 X%1$f Y%2$f\n", //$NON-NLS-1$
				displacementCommand.getY(), displacementCommand.getX()));
	}

	@Override
	public void visit(int index, CutToCommand cutToCommand) throws Exception {
		throw new Exception("cannot export punch gcode with cut command (lazer specific commands)");
	}

	public List<String> getGCODECommands() {
		return grblCommands;
	}

	@Override
	public void visit(int index, HomingCommand command) throws Exception {
		grblCommands.add("$H\n"); //$NON-NLS-1$
	}
	
	@Override
	public List<String> getEndingCommands() {
		return (List<String>)Collections.EMPTY_LIST;
	}
	
	@Override
	public List<String> getPreludeCommands() {
		return (List<String>)Collections.EMPTY_LIST;
	}

}
