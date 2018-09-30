package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

/**
 * Compte the range of the punchplan
 *
 * @author use
 */
public class RangeVisitor extends CommandVisitor {

  private double xmin = Double.NaN;
  private double xmax = Double.NaN;
  private double ymin = Double.NaN;
  private double ymax = Double.NaN;

  public RangeVisitor() {}

  public double getXmax() {
    return xmax;
  }

  public double getXmin() {
    return xmin;
  }

  public double getYmin() {
    return ymin;
  }

  public double getYmax() {
    return ymax;
  }

  @Override
  public void visit(int index, PunchCommand punchCommand) throws Exception {
    xmin = Math.min(xmin, punchCommand.getX());
    xmax = Math.max(xmax, punchCommand.getX());
    ymin = Math.min(ymin, punchCommand.getY());
    ymax = Math.max(ymax, punchCommand.getY());
  }

  @Override
  public void visit(int index, DisplacementCommand displacementCommand) throws Exception {
    xmin = Math.min(xmin, displacementCommand.getX());
    xmax = Math.max(xmax, displacementCommand.getX());
    ymin = Math.min(ymin, displacementCommand.getY());
    ymax = Math.max(ymax, displacementCommand.getY());
  }

  @Override
  public void visit(int index, HomingCommand command)
      throws Exception { // TODO Auto-generated method stub
  }
}
