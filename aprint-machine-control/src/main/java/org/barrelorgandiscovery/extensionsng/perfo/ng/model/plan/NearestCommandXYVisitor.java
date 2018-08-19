package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

public class NearestCommandXYVisitor extends CommandVisitor {

	private double x;
	private double y;

	private double distance = Double.MAX_VALUE;
	private XYCommand nearest = null;
	private Integer index = null;

	public NearestCommandXYVisitor(double x, double y) {
		this.x = x;
		this.y = y;
	}

	private double distanceTo(XYCommand xycommand) {
		return Math.sqrt(Math.pow(xycommand.getX() - x, 2)
				+ Math.pow(xycommand.getY() - y, 2));
	}

	private void evaluateNearest(int index , XYCommand xycommand) {
		double d = distanceTo(xycommand);
		if (d < distance) {
			distance = d;
			nearest = xycommand;
			this.index = index;
		}
	}

	@Override
	public void visit(int index,PunchCommand punchCommand) throws Exception {
		evaluateNearest(index, punchCommand);
	}

	@Override
	public void visit(int index,DisplacementCommand displacementCommand) throws Exception {
		evaluateNearest(index, displacementCommand);
	}
	
	@Override
	public void visit(int index,HomingCommand command) throws Exception {
		
		
	}

	public Integer getIndex() {
		return index;
	}
	
	public XYCommand getNearest() {
		return nearest;
	}

}
