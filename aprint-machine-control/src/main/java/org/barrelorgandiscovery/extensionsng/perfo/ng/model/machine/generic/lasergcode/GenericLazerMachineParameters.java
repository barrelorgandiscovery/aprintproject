package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.generic.lasergcode;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

/**
 * GRBL machine that communicate with com port
 * 
 * @author pfreydiere
 * 
 */
public class GenericLazerMachineParameters extends AbstractMachineParameters {

	private static final long serialVersionUID = -432272629674421648L;

	private static final String MAX_SPEED = "maxSpeed"; //$NON-NLS-1$
	private static final String MAX_POWER = "maxPower"; //$NON-NLS-1$

	// configuration of the commands
	private static final String HOMINGCOMMANDS = "homingCommands"; //$NON-NLS-1$
	private static final String STARTBOOKPRECOMMANDS = "startBookPrecommands"; //$NON-NLS-1$

	private static final String DISPLACEMENTPRECOMMAND = "displacementPreCommand";//$NON-NLS-1$
	private static final String DISPLACEMENTCOMMANDPATTERN = "displacementCommandPattern";//$NON-NLS-1$
	private static final String DISPLACEMENTPOSTCOMMAND = "displacementPostCommand";//$NON-NLS-1$

	private static final String CUTTINGPRECOMMAND = "cuttingPreCommand";//$NON-NLS-1$
	private static final String CUTTINGTOCOMMANDPATTERN = "cuttingToCommandPattern";//$NON-NLS-1$
	private static final String CUTTINGPOSTCOMMAND = "cuttingPostCommand";//$NON-NLS-1$

	private static final String POWERCHANGECOMMAND = "powerChangeCommand";//$NON-NLS-1$

	private static final String ENDBOOKPRECOMMANDS = "endBookPrecommands";//$NON-NLS-1$

	Integer maxspeed = 1000;
	Integer maxPower = 1000;

	GenericLazerGCodeParameterGeneration generation = new GenericLazerGCodeParameterGeneration();

	public GenericLazerGCodeParameterGeneration getGenerationParameters() {
		return generation;
	}
	
	public Integer getMaxspeed() {
		return maxspeed;
	}

	public void setMaxspeed(Integer maxspeed) {
		assert maxspeed != null;
		assert maxspeed > 0;
		this.maxspeed = maxspeed;
	}

	public void setMaxPower(Integer maxPower) {
		this.maxPower = maxPower;
	}

	public Integer getMaxPower() {
		return maxPower;
	}

	public String getCuttingPostCommand() {
		return generation.cuttingPostCommand;
	}

	public String getCuttingPreCommand() {
		return generation.cuttingPreCommand;
	}

	public String getCuttingToCommandPattern() {
		return generation.cuttingToCommandPattern;
	}

	public String getDisplacementCommandPattern() {
		return generation.displacementCommandPattern;
	}

	public String getDisplacementPostCommand() {
		return generation.displacementPostCommand;
	}

	public String getDisplacementPreCommand() {
		return generation.displacementPreCommand;
	}

	public String getEndBookPrecommands() {
		return generation.endBookPrecommands;
	}

	public String getPowerChangeCommand() {
		return generation.powerChangeCommand;
	}

	public String getHomingCommands() {
		return generation.homingCommands;
	}

	public String getStartBookPrecommands() {
		return generation.startBookPrecommands;
	}

	public void setCuttingPostCommand(String cuttingPostCommand) {
		this.generation.cuttingPostCommand = cuttingPostCommand;
	}

	public void setCuttingPreCommand(String cuttingPreCommand) {
		generation.cuttingPreCommand = cuttingPreCommand;
	}

	public void setCuttingToCommandPattern(String cuttingToCommandPattern) {
		generation.cuttingToCommandPattern = cuttingToCommandPattern;
	}

	public void setDisplacementCommandPattern(String displacementCommandPattern) {
		generation.displacementCommandPattern = displacementCommandPattern;
	}

	public void setDisplacementPostCommand(String displacementPostCommand) {
		generation.displacementPostCommand = displacementPostCommand;
	}

	public void setDisplacementPreCommand(String displacementPreCommand) {
		generation.displacementPreCommand = displacementPreCommand;
	}

	public void setEndBookPrecommands(String endBookPrecommands) {
		generation.endBookPrecommands = endBookPrecommands;
	}

	public void setPowerChangeCommand(String powerChangeCommand) {
		generation.powerChangeCommand = powerChangeCommand;
	}

	public void setHomingCommands(String homingCommands) {
		generation.homingCommands = homingCommands;
	}

	public void setStartBookPrecommands(String startBookPrecommands) {
		generation.startBookPrecommands = startBookPrecommands;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.
	 * AbstractMachineParameters#getLabelName()
	 */
	@Override
	public String getLabelName() {
		return "Generic Lazer Machine GCode";
	}
	

	@Override
	public void loadParameters(IPrefsStorage ps) throws Exception {
		assert ps != null;

		maxspeed = ps.getIntegerProperty(MAX_SPEED, 1000);
		maxPower = ps.getIntegerProperty(MAX_POWER, 1000);

		// configuration of the commands
		setHomingCommands(
				ps.getStringProperty(HOMINGCOMMANDS, new GenericLazerGCodeParameterGeneration().homingCommands));

		setStartBookPrecommands(ps.getStringProperty(STARTBOOKPRECOMMANDS,
				new GenericLazerGCodeParameterGeneration().startBookPrecommands));
		
		setDisplacementPreCommand(ps.getStringProperty(DISPLACEMENTPRECOMMAND,
				new GenericLazerGCodeParameterGeneration().displacementPreCommand));

		setDisplacementCommandPattern(ps.getStringProperty(DISPLACEMENTCOMMANDPATTERN,
				new GenericLazerGCodeParameterGeneration().displacementCommandPattern));

		setDisplacementPostCommand(ps.getStringProperty(DISPLACEMENTPOSTCOMMAND,
				new GenericLazerGCodeParameterGeneration().displacementPostCommand));

		setCuttingPreCommand(
				ps.getStringProperty(CUTTINGPRECOMMAND, new GenericLazerGCodeParameterGeneration().cuttingPreCommand));
		setCuttingToCommandPattern(ps.getStringProperty(CUTTINGTOCOMMANDPATTERN,
				new GenericLazerGCodeParameterGeneration().cuttingToCommandPattern));

		setCuttingPostCommand(ps.getStringProperty(CUTTINGPOSTCOMMAND,
				new GenericLazerGCodeParameterGeneration().cuttingPostCommand));

		setPowerChangeCommand(ps.getStringProperty(POWERCHANGECOMMAND,
				new GenericLazerGCodeParameterGeneration().powerChangeCommand));

		setEndBookPrecommands(ps.getStringProperty(ENDBOOKPRECOMMANDS,
				new GenericLazerGCodeParameterGeneration().endBookPrecommands));

	}

	@Override
	public void saveParameters(IPrefsStorage ps) throws Exception {
		assert ps != null;
		ps.setIntegerProperty(MAX_SPEED, maxspeed);
		ps.setIntegerProperty(MAX_POWER, maxPower);

		ps.setStringProperty(HOMINGCOMMANDS, getHomingCommands());

		ps.setStringProperty(STARTBOOKPRECOMMANDS, getStartBookPrecommands());

		ps.setStringProperty(DISPLACEMENTPRECOMMAND, getDisplacementPreCommand());

		ps.setStringProperty(DISPLACEMENTCOMMANDPATTERN, getDisplacementCommandPattern());

		ps.setStringProperty(DISPLACEMENTPOSTCOMMAND, getDisplacementPostCommand());

		ps.setStringProperty(CUTTINGTOCOMMANDPATTERN, getCuttingToCommandPattern());

		ps.setStringProperty(CUTTINGPRECOMMAND, getCuttingPreCommand());

		ps.setStringProperty(CUTTINGPOSTCOMMAND, getCuttingPostCommand());

		ps.setStringProperty(POWERCHANGECOMMAND, getPowerChangeCommand());

		ps.setStringProperty(ENDBOOKPRECOMMANDS, getEndBookPrecommands());

	}

}
