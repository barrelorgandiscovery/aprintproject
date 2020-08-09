package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

public class MovePlanVisitor extends CommandVisitor {

	private double deltaX;
	private double deltaY;

	private PunchPlan constructedPunchPlan = new PunchPlan();

	public MovePlanVisitor(PunchPlan punchPlan, double deltaX, double deltaY) {
		this.constructedPunchPlan = new PunchPlan();
		this.constructedPunchPlan.setOptimizerClass(punchPlan.getOptimizerClass());
		this.constructedPunchPlan.setOptimizerParameters(punchPlan.getOptimizerParmeters());
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	@Override
	public void visit(int index, PunchCommand punchCommand) throws Exception {

		constructedPunchPlan.getCommandsByRef()
				.add(new PunchCommand(punchCommand.getX() + deltaX, punchCommand.getY() + deltaY));
	}

	@Override
	public void visit(int index, DisplacementCommand displacementCommand) throws Exception {

		constructedPunchPlan.getCommandsByRef()
				.add(new DisplacementCommand(displacementCommand.getX() + deltaX, displacementCommand.getY() + deltaY));
	}

	@Override
	public void visit(int index, CutToCommand cutToCommand) throws Exception {
		constructedPunchPlan.getCommandsByRef().add(new CutToCommand(cutToCommand.getX() + deltaX,
				cutToCommand.getY() + deltaY, cutToCommand.getPowerFactor(), cutToCommand.getSpeed()));
	}

	@Override
	public void visit(int index, HomingCommand command) throws Exception {
		constructedPunchPlan.getCommandsByRef().add(command);
	}

	/**
	 * result moved punch plan
	 *
	 * @return
	 */
	public PunchPlan getConstructedPunchPlan() {
		return constructedPunchPlan;
	}
}
