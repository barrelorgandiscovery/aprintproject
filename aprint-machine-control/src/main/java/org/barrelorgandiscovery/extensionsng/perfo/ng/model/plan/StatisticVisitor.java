package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;

public class StatisticVisitor extends CommandVisitor {

	private long nbdeplacement;
	private double distanceDeplacement;
	private long nbPunch;

	private double currentX;
	private double currentY;

	private boolean html;

	public StatisticVisitor(boolean html) {
		this.html = html;
	}

	private void computeLengthAndUpdateCurrentPos(XYCommand x) {
		distanceDeplacement += Math.sqrt(Math.pow(currentX - x.getX(), 2)
				+ Math.pow(currentY - x.getY(), 2));
		currentX = x.getX();
		currentY = x.getY();
		nbdeplacement++;
	}

	@Override
	public void visit(int index,HomingCommand command) throws Exception {
		// nothing to do for homing	
	}
	
	@Override
	public void visit(int index,PunchCommand punchCommand) throws Exception {
		computeLengthAndUpdateCurrentPos(punchCommand);
		nbPunch++;
	}

	@Override
	public void visit(int index,DisplacementCommand displacementCommand) throws Exception {
		computeLengthAndUpdateCurrentPos(displacementCommand);
		
	}

	public StringBuilder getReport() {
		StringBuilder sb = new StringBuilder();
		sb.append(Messages.getString("StatisticVisitor.0")); //$NON-NLS-1$

		if (html)
			sb.append("<b>"); //$NON-NLS-1$

		sb.append(nbPunch);
		if (html)
			sb.append("</b>").append("<br/>"); //$NON-NLS-1$ //$NON-NLS-2$

		sb.append("   ").append(Messages.getString("StatisticVisitor.5")); //$NON-NLS-1$ //$NON-NLS-2$

		if (html)
			sb.append("<b>"); //$NON-NLS-1$

		sb.append(String.format(" : %1$d m (%2$.2f mm)", (int)(distanceDeplacement/1000), distanceDeplacement)); //$NON-NLS-1$
		
		if (html)
			sb.append(Messages.getString("StatisticVisitor.8")); //$NON-NLS-1$

		sb.append("\n");

		return sb;
	}
	
	public double getDistanceDeplacement() {
		return distanceDeplacement;
	}
	
	public long getNbPunch() {
		return nbPunch;
	}
	
}
