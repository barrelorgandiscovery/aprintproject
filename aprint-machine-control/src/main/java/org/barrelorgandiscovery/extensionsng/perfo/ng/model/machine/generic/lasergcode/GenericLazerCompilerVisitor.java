package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.generic.lasergcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GCodeCompiler;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CutToCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.XYCommand;

/**
 * transform generic commands into GCode orders
 * 
 * @author pfreydiere
 * 
 */
public class GenericLazerCompilerVisitor extends GCodeCompiler {

	private ArrayList<String> gcodeCommands = new ArrayList<>();

	private int maxspeed;
	private int maxPower;

	private int currentSpeed;
	private int currentPower;

	private Double currentPositionX;
	private Double currentPositionY;

	private GenericLazerGCodeParameterGeneration generationParameters;

	public GenericLazerCompilerVisitor(int maxspeed, int maxpower,
			GenericLazerGCodeParameterGeneration generationParameters) {
		this.maxspeed = maxspeed;
		this.maxPower = maxpower;
		assert generationParameters != null;
		this.generationParameters = generationParameters;
	}

	@Override
	public void reset() {
		gcodeCommands = new ArrayList<>();
		currentPositionX = null;
		currentPositionY = null;
	}

	@Override
	public void visit(int index, PunchCommand punchCommand) throws Exception {
		throw new Exception("cannot use the punch command for this machine");
	}

	private static boolean sameWithEpsilon(double a, double b) {
		return Math.abs(a - b) < 1e-8;
	}

	/**
	 * this function check if the current position is the same as the current one if
	 * same position, this function returns true if false, this function update the
	 * current position
	 * 
	 * @param xyCommand
	 * @return
	 */
	private boolean checkSamePositionOtherwiseUpdateCurrentPosition(XYCommand xyCommand) {
		if (currentPositionX != null && currentPositionY != null) {
			// check if we are on the same position
			if (sameWithEpsilon(xyCommand.getX(), currentPositionX)
					&& sameWithEpsilon(xyCommand.getY(), currentPositionY)) {
				return true;
			}
		}

		currentPositionX = xyCommand.getX();
		currentPositionY = xyCommand.getY();

		return false;

	}

	@Override
	public void visit(int index, DisplacementCommand displacementCommand) throws Exception {
		if (!checkSamePositionOtherwiseUpdateCurrentPosition(displacementCommand)) {
			
			addCommand(generationParameters.displacementPreCommand);
			String commandGeneration = String.format(Locale.ENGLISH, generationParameters.displacementCommandPattern, // $NON-NLS-1$
					displacementCommand.getY(), displacementCommand.getX());
			addCommand(commandGeneration);
			addCommand(generationParameters.displacementPostCommand);

		}
	}

	@Override
	public void visit(int index, CutToCommand cutToCommand) throws Exception {

		int commandspeed = (int) (cutToCommand.getSpeedFactor() * maxspeed);
		if (commandspeed <= 0)
			throw new Exception("bad computed speed :" + commandspeed);

		int powercommand = (int) (cutToCommand.getPowerFactor() * maxPower);
		if (powercommand <= 0) {
			throw new Exception("command cut has null power factor :" + cutToCommand);
		}

		// update current position
		if (!checkSamePositionOtherwiseUpdateCurrentPosition(cutToCommand)) {
			if (currentPower != powercommand) {

				// add power change
				String commandGeneration = String.format(Locale.ENGLISH, generationParameters.powerChangeCommand, // $NON-NLS-1$
						powercommand);

				if (commandGeneration != null && !commandGeneration.isBlank()) {
					addCommand(commandGeneration);
				}

				this.currentPower = powercommand;
			}

			int speedcommand = (int) (cutToCommand.getSpeedFactor() * maxspeed);
			if (speedcommand <= 0) {
				throw new Exception("command cut has null speed :" + cutToCommand);
			}

			if (generationParameters.cuttingPreCommand != null && !generationParameters.cuttingPreCommand.isBlank()) {
				addCommand(generationParameters.cuttingPreCommand);
			}

			String commandGeneration = String.format(Locale.ENGLISH, generationParameters.cuttingToCommandPattern, // $NON-NLS-1$
					cutToCommand.getY(), cutToCommand.getX(), speedcommand, powercommand);

			assert commandGeneration != null;
			if (!commandGeneration.isBlank()) {
				addCommand(commandGeneration);
			}

			if (generationParameters.cuttingPostCommand != null && !generationParameters.cuttingPostCommand.isBlank()) {
				addCommand(generationParameters.cuttingPostCommand);
			}
		}
	}

	public List<String> getGCODECommands() {
		return gcodeCommands;
	}

	@Override
	public void visit(int index, HomingCommand command) throws Exception {
		if (generationParameters.homingCommands != null && !generationParameters.homingCommands.isBlank()) {
			addCommand(generationParameters.homingCommands); // $NON-NLS-1$
		}
	}

	@Override
	public List<String> getPreludeCommands() {
		List<String> list = new ArrayList<String>();
		if (generationParameters.startBookPrecommands != null && !generationParameters.startBookPrecommands.isBlank()) {
			list.add(generationParameters.startBookPrecommands + "\n");
		}
		return list;
	}

	@Override
	public List<String> getEndingCommands() {
		ArrayList<String> list = new ArrayList<String>();
		if (generationParameters.endBookPrecommands != null && !generationParameters.endBookPrecommands.isBlank()) {
			list.add(generationParameters.endBookPrecommands + "\n");
		}

		return list;
	}

	private void addCommand(String command) {
		if (command == null || command.isBlank() || command.isEmpty()) {
			return;
		}
		gcodeCommands.add(command + "\n");
	}

}
