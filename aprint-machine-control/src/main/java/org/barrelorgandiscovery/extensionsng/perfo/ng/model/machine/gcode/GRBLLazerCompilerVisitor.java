package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CommandVisitor;
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
public class GRBLLazerCompilerVisitor extends GCodeCompiler {

	private ArrayList<String> grblCommands = new ArrayList<>();

	private int maxspeed;
	private int maxPower;
	
	private int currentSpeed;
	private int currentPower;

	public GRBLLazerCompilerVisitor(int maxspeed, int maxpower) {
		this.maxspeed = maxspeed;
		this.maxPower = maxpower;
	}

	@Override
	public void reset() {
		grblCommands = new ArrayList<>();
	}

	@Override
	public void visit(int index, PunchCommand punchCommand) throws Exception {
		throw new Exception("cannot use the punch command for this machine");
	}

	@Override
	public void visit(int index, DisplacementCommand displacementCommand) throws Exception {
		grblCommands.add(String.format(Locale.ENGLISH, "G0 X%1$f Y%2$f\n", //$NON-NLS-1$
				displacementCommand.getY(), displacementCommand.getX()));
	}

	@Override
	public void visit(int index, CutToCommand cutToCommand) throws Exception {
		int commandspeed = (int) (cutToCommand.getSpeedFactor() * maxspeed);
		if (commandspeed <= 0)
			throw new Exception("bad computed speed :" + commandspeed);
		
		int powercommand = (int)cutToCommand.getPowerFactor() * maxPower;
		if (powercommand <= 0) {
			throw new Exception("command cut has null power factor :" + cutToCommand);
		}
		
		if (currentPower != powercommand) {
			grblCommands.add(String.format(Locale.ENGLISH, "S%1$d\n", //$NON-NLS-1$
					powercommand));
			this.currentPower = powercommand;
		}
		
		int speedcommand = (int)(cutToCommand.getSpeedFactor()*maxspeed);
		if (speedcommand <= 0) {
			throw new Exception("command cut has null speed :" + cutToCommand);
		}
		
		grblCommands.add(String.format(Locale.ENGLISH, "G1 X%1$f Y%2$f F%3$d\n", //$NON-NLS-1$
				cutToCommand.getY(), cutToCommand.getX(), speedcommand));
	}

	public List<String> getGCODECommands() {
		return grblCommands;
	}

	@Override
	public void visit(int index, HomingCommand command) throws Exception {
		grblCommands.add("$H\n"); //$NON-NLS-1$
		// activate the lazer
		grblCommands.add("M3\n"); //$NON-NLS-1$
		
	}

}