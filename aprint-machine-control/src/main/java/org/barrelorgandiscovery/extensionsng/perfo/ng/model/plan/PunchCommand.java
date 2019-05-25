package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import org.barrelorgandiscovery.gui.atrace.Punch;
import org.barrelorgandiscovery.tools.HashCodeUtils;

public class PunchCommand extends Command implements XYCommand {

	
	private double x;
	private double y;
	
	/**
	 * PunchCommand, we are in screen cartesian coordinates
	 * @param x
	 * @param y
	 */
	public PunchCommand(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	@Override
	public double getX() {
		return x;
	}
	
	@Override
	public double getY() {
		return y;
	}
	
	@Override
	public void accept(int index,CommandVisitor visitor) throws Exception {
		visitor.visit(index, this);
	}
	
	@Override
	public int hashCode() {
		int s = HashCodeUtils.hash(HashCodeUtils.SEED, x);
		s = HashCodeUtils.hash(s, y);
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		PunchCommand n = (PunchCommand) obj;
		return x == n.x && y == n.y;
	}
	
}
