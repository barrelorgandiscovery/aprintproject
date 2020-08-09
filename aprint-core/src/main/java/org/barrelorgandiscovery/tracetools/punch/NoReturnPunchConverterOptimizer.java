package org.barrelorgandiscovery.tracetools.punch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import javax.swing.ImageIcon;

import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.atrace.Optimizer;
import org.barrelorgandiscovery.gui.atrace.OptimizerProgress;
import org.barrelorgandiscovery.gui.atrace.OptimizerResult;
import org.barrelorgandiscovery.gui.atrace.Punch;
import org.barrelorgandiscovery.gui.atrace.PunchConverter;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class NoReturnPunchConverterOptimizer implements Optimizer<Punch> {

  private static class PunchComparator implements Comparator<Punch> {

    private boolean naturalOrder = true;

    public PunchComparator(boolean order) {
      this.naturalOrder = order;
    }

    @Override
    public int compare(Punch o1, Punch o2) {

      if (naturalOrder) {
        int r = Double.compare(o1.y, o2.y);
        if (r != 0) {
          return r;
        }

        return Integer.compare(o1.hashCode(), o2.hashCode());
      }

      // reverse
      int r = Double.compare(o2.y, o1.y);
      if (r != 0) return r;
      return Integer.compare(o1.hashCode(), o2.hashCode());
    }
  }

  private NoReturnPunchConverterOptimizerParameters parameters = new NoReturnPunchConverterOptimizerParameters();

  /** default constructor */
  public NoReturnPunchConverterOptimizer() {}

  public NoReturnPunchConverterOptimizer(NoReturnPunchConverterOptimizerParameters parameters) {
    assert parameters != null;
    this.parameters = parameters;
  }

  @Override
  public Object getDefaultParameters() {
    return parameters;
  }

  @Override
  public String getTitle() {
    return "U Optimizer";
  }

  @Override
  public ImageIcon getIcon() {
    // no icon yet
    return null;
  }

  @Override
  public OptimizerResult<Punch> optimize(VirtualBook carton) throws Exception {
    return optimize(carton, null, null);
  }

  @Override
  public OptimizerResult<Punch> optimize(VirtualBook carton, OptimizerProgress progress, ICancelTracker ct)
      throws Exception {

    assert carton != null;

    PunchConverter pc =
        new PunchConverter(
            carton.getScale(),
            parameters.getPunchWidth(),
            parameters.getOverlap(),
            parameters.getNotPunchedIfLessThan());

    OptimizerResult<Punch> convert = pc.convert(carton.getOrderedHolesCopy());

    ArrayList<Punch> result = orderPunches(convert.result);

    convert.result = result.toArray(new Punch[result.size()]);

    return convert;
  }

  public ArrayList<Punch> orderPunches(Punch[] punches) { // order the punches by x

    Arrays.sort(
        punches,
        new Comparator<Punch>() {
          @Override
          public int compare(Punch o1, Punch o2) {

            int r = Double.compare(o1.x, o2.x);
            if (r != 0) return r;
            return Double.compare(o1.y, o2.y);
          }
        });

    // make go and forth

    boolean updownDirection = true;

    ArrayList<Punch> result = new ArrayList<>();
    LinkedList<Punch> list = new LinkedList<>(Arrays.asList(punches));

    while (list.size() > 0) {
      ArrayList<Punch> currentList = new ArrayList<>();
      Punch current = null;
      do {
        if (current == null) {
          current = list.pop();
          currentList.add(current);
        } else {

          currentList.add(list.pop());
        }

      } while (list.size() > 0 && current.x == list.peekFirst().x);

      // all elements in list belong to the same x
      currentList.sort(new PunchComparator(updownDirection));
      // depending on the direction, sort by the way
      result.addAll(currentList);
      updownDirection = !updownDirection;
    }
    return result;
  }
}
