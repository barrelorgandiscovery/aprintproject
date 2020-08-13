package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * displacement command, the X is the FIRST AXIS of the machine, 
 * the Y, the second one
 * @author pfreydiere
 *
 */
public class DisplacementCommand extends Command implements XYCommand {

	private double moveToX;
	private double moveToY;

	public DisplacementCommand(double moveToX, double moveToY) {
		this.moveToX = moveToX;
		this.moveToY = moveToY;
	}

	@Override
	public double getX() {
		return moveToX;
	}

	@Override
	public double getY() {
		return moveToY;
	}

	@Override
	public void accept(int index, CommandVisitor visitor) throws Exception {
		visitor.visit(index, this);
	}

	@Override
	public int hashCode() {
		int s = HashCodeUtils.hash(HashCodeUtils.SEED, moveToX);
		s = HashCodeUtils.hash(s, moveToY);
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		DisplacementCommand n = (DisplacementCommand) obj;
		return moveToX == n.moveToX && moveToY == n.moveToY;
	}

}
