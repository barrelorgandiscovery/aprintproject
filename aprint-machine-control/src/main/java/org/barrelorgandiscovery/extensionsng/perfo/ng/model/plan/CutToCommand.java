package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * this command used for lazer, design a cut to line command
 * 
 * @author pfreydiere
 *
 */
public class CutToCommand extends Command implements XYCommand {

	private double moveToX;
	private double moveToY;
	private double powerFactor;
	private double speed;

	public CutToCommand(double moveToX, double moveToY, double powerFactor, double speed) {
		this.moveToX = moveToX;
		this.moveToY = moveToY;
		this.powerFactor = powerFactor;
		this.speed = speed;
	}

	@Override
	public double getX() {
		return moveToX;
	}

	@Override
	public double getY() {
		return moveToY;
	}

	public double getPowerFactor() {
		return powerFactor;
	}

	public double getSpeedFactor() {
		return speed;
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

		CutToCommand n = (CutToCommand) obj;
		return moveToX == n.moveToX && moveToY == n.moveToY;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CutTo Command ").append(moveToX).append(',').append(moveToY).append("  Power :").append(powerFactor)
				.append("  Speed :").append(speed);
		return sb.toString();
	}

}
