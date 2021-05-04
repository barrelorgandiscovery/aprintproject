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
    xmin = minNotNan(xmin, punchCommand.getX());
    xmax = maxNotNan(xmax, punchCommand.getX());
    ymin = minNotNan(ymin, punchCommand.getY());
    ymax = maxNotNan(ymax, punchCommand.getY());
  }

  @Override
  public void visit(int index, DisplacementCommand displacementCommand) throws Exception {
    xmin = minNotNan(xmin, displacementCommand.getX());
    xmax = maxNotNan(xmax, displacementCommand.getX());
    ymin = minNotNan(ymin, displacementCommand.getY());
    ymax = maxNotNan(ymax, displacementCommand.getY());
  }
  
  @Override
  public void visit(int index, CutToCommand cutToCommand) throws Exception {
    xmin = minNotNan(xmin, cutToCommand.getX());
    xmax = maxNotNan(xmax, cutToCommand.getX());
    ymin = minNotNan(ymin, cutToCommand.getY());
    ymax = maxNotNan(ymax, cutToCommand.getY());
  }
  
  private double minNotNan(double a,double b) {
	  if (Double.isNaN(a))
		  return b;
	  if (Double.isNaN(b))
		  return a;
	  return Math.min(a,b);
  }

  private double maxNotNan(double a,double b) {
	  if (Double.isNaN(a))
		  return b;
	  if (Double.isNaN(b))
		  return a;
	  return Math.max(a,b);
  }

  
  @Override
  public void visit(int index, HomingCommand command)
      throws Exception { // TODO Auto-generated method stub
  }
}
