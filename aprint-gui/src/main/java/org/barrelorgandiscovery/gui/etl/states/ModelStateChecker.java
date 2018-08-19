package org.barrelorgandiscovery.gui.etl.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.Model;
import org.barrelorgandiscovery.model.ModelLink;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.ModelVisitor;

public class ModelStateChecker extends ModelVisitor {

  private static Logger logger = Logger.getLogger(ModelStateChecker.class);

  // Constants of the cell states

  private HashMap<String, CellState> states = new HashMap<String, CellState>();

  private ArrayList<String> errors = new ArrayList<>();

  @Override
  public void visit(Model model, ModelParameter parameter) {

    String paramid = parameter.getId();
    assert paramid != null;
    assert !"".equals(paramid); //$NON-NLS-1$

    CellState status;

    if (parameter.isIn()) {
      status = CellState.INPUTPARAMETEROK;
      // check if the parameter is connected to a link
      if (model.getLinksConnectedToParameter(parameter, false).size() <= 0
          && parameter.isOptional() == false) {
        status = CellState.MANDATORY_INPUTPARAMETER_UNCONNECTED;
        errors.add(Messages.getString("ModelStateChecker.1") + parameter.getLabel() + Messages.getString("ModelStateChecker.2") + parameter.getStep().getLabel() + Messages.getString("ModelStateChecker.3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    } else {
      status = CellState.OUTPUTPARAMETEROK;
    }

    logger.debug("associate state " + status + " to paramid :" + paramid); //$NON-NLS-1$ //$NON-NLS-2$
    states.put(paramid, status);
  }

  @Override
  public void visit(Model model, ModelLink link) {
    // nothing to do on links
  }

  @Override
  public void visit(Model model, ModelStep step) {

    CellState state = CellState.STEP_OK;

    if (step.isConfigured()) {

      // check step is scheduled

      if (step.getScheduleOrder() == -1) {
        state = CellState.STEP_UNSCHEDULED;
      }

    } else {
      state = CellState.STEP_UNCONFIGURED;
      errors.add(Messages.getString("ModelStateChecker.6") + step.getLabel() + Messages.getString("ModelStateChecker.7")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    states.put(step.getId(), state);
  }

  /**
   * get the states
   *
   * @return
   */
  public Map<String, CellState> getStates() {
    return states;
  }

  public ArrayList<String> getErrors() {
    return errors;
  }
}
