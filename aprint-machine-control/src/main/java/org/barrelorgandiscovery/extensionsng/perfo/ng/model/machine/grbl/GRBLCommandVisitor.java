package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CommandVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;

/**
 * transform generic commands into GCode orders
 * 
 * @author pfreydiere
 * 
 */
public class GRBLCommandVisitor extends CommandVisitor {

	private ArrayList<String> grblCommands = new ArrayList<>();

	public GRBLCommandVisitor() {

	}

	@Override
	public void visit(int index,PunchCommand punchCommand) throws Exception {
		grblCommands.add(String.format(Locale.ENGLISH, "G90 X%1$f Y%2$f\n", //$NON-NLS-1$
				punchCommand.getY(), punchCommand.getX()));
		grblCommands.add("M100\n"); //$NON-NLS-1$
	}

	@Override
	public void visit(int index,DisplacementCommand displacementCommand) throws Exception {
		grblCommands.add(String.format(Locale.ENGLISH, "G90 X%1$f Y%2$f\n", //$NON-NLS-1$
				displacementCommand.getY(), displacementCommand.getX()));
	}

	public List<String> getGRBLCommands() {
		return grblCommands;
	}

	@Override
	public void visit(int index,HomingCommand command) throws Exception {
		grblCommands.add("$H\n"); //$NON-NLS-1$
	}

}
