package org.barrelorgandiscovery.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.model.comparators.ModelLinkComparator;
import org.barrelorgandiscovery.model.comparators.ModelParameterComparator;
import org.barrelorgandiscovery.model.comparators.ModelStepComparator;

/**
 * Model mainting a processing model, a collection of modelstep and modellink
 * between model parameters
 * 
 * @author pfreydiere
 * 
 */
public class Model implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3669380321992581308L;

	private static Logger logger = Logger.getLogger(Model.class);

	AbstractParameter outParameter;

	Set<ModelStep> steps = new TreeSet<ModelStep>(new ModelStepComparator());

	Set<ModelLink> links = new TreeSet<ModelLink>(new ModelLinkComparator());

	/**
	 * Steps ordered by schedule
	 */
	Set<ModelStep> orderedScheduleSteps = null;

	public Model() {

	}

	/**
	 * 
	 * @param step
	 * @throws Exception
	 */
	public void addModelStep(ModelStep step) throws Exception {
		checkModelStep(step);
		steps.add(step);
		invalidateSchedule();
	}

	/**
	 * Add link to the model
	 * 
	 * @param link
	 * @throws Exception
	 *             if the link is not properly associated to the model
	 */
	public void addModelLink(ModelLink link) throws Exception {
		AbstractParameter from = link.getFrom();
		if (from == null)
			throw new Exception("null from field");
		if (!steps.contains(from.getStep()))
			throw new Exception("from step is not in model");
		AbstractParameter to = link.getTo();
		if (to == null)
			throw new Exception("null to field");
		if (!steps.contains(to.getStep()))
			throw new Exception("to step is not in model");
		links.add(link);
		invalidateSchedule();
	}

	/**
	 * internal method for checking the internal step
	 * 
	 * @param step
	 * @throws Exception
	 */
	private void checkModelStep(ModelStep step) throws Exception {
		AbstractParameter[] p = step.getAllParametersByRef();
		for (int i = 0; i < p.length; i++) {
			AbstractParameter modelParameter = p[i];
			if (modelParameter.getStep() != step)
				throw new Exception("error, parameter " + modelParameter + " is not associated to " + step);
		}
	}

	/**
	 * Remove the step and disconnect from existing links
	 * 
	 * @param step
	 * @throws Exception
	 */
	public void removeStepAndDisconnectLinks(ModelStep step) throws Exception {

		if (!steps.contains(step))
			throw new Exception("step " + step + " is not in the model");

		Set<ModelLink> allLinksAssociatedTo = getAllLinksAssociatedTo(step);

		links.removeAll(allLinksAssociatedTo);
		steps.remove(step);

		invalidateSchedule();
	}

	/**
	 * Remove link, this method invalidate the schedule
	 * 
	 * @param link
	 */
	public void removeLink(ModelLink link) {
		links.remove(link);
		invalidateSchedule();
	}

	/**
	 * Remove link by its id
	 * 
	 * @param id
	 */
	public void removeLinkById(String id) {
		if (id == null)
			return;
		ModelLink mlremove = null;
		for (Iterator iterator = links.iterator(); iterator.hasNext();) {
			ModelLink ml = (ModelLink) iterator.next();
			if (id.equals(ml.getId())) {
				mlremove = ml;
				break;
			}
		}
		if (mlremove != null)
			links.remove(mlremove);
	}

	/**
	 * 
	 */
	private void invalidateSchedule() {
		orderedScheduleSteps = null;
		razSchedule();
	}

	/**
	 * List links associated to a step (origin or destination)
	 */
	public Set<ModelLink> getAllLinksAssociatedTo(ModelStep step) {
		TreeSet<ModelLink> l = new TreeSet<ModelLink>(new ModelLinkComparator());
		for (ModelLink m : links) {
			if (m.getFrom().getStep() == step) {
				l.add(m);
			} else if (m.getTo().getStep() == step) {
				l.add(m);
			}
		}
		return l;
	}

	/**
	 * 
	 * @param param
	 * @return
	 */
	public Set<ModelLink> getLinksConnectedToParameter(ModelParameter param, boolean source) {

		TreeSet<ModelLink> ts = new TreeSet<ModelLink>(new ModelLinkComparator());

		if (param == null)
			return ts;

		for (Iterator iterator = links.iterator(); iterator.hasNext();) {
			ModelLink l = (ModelLink) iterator.next();
			if (source && (l.getFrom() == param)) {
				ts.add(l);
			} else if ((!source) && (l.getTo() == param)) {
				ts.add(l);
			}
		}
		return ts;

	}

	/**
	 * Get all the link that are linked to the destination step "step". this
	 * does not depend on the kind of parameter
	 * 
	 * @param step
	 * @return a set containing the steps
	 */
	public Set<ModelLink> getPrecedingLinksAssociatedTo(ModelStep step) {
		TreeSet<ModelLink> l = new TreeSet<ModelLink>(new ModelLinkComparator());
		for (ModelLink m : links) {
			if (m.getTo().getStep() == step) {
				l.add(m);
			}
		}
		return l;
	}

	/**
	 * 
	 * @param step
	 * @return
	 */
	public Set<ModelLink> getFollowingLinksAssociatedTo(ModelStep step) {
		TreeSet<ModelLink> l = new TreeSet<ModelLink>(new ModelLinkComparator());
		for (ModelLink m : links) {
			if (m.getFrom().getStep() == step) {
				l.add(m);
			}
		}
		return l;
	}

	/**
	 * Is the schedule done ?
	 * 
	 * @return
	 */
	public boolean isScheduled() {
		return orderedScheduleSteps != null;
	}

	/**
	 * Compute internal step scheduling from the sink node specified in
	 * parameter, the internal orderedScheduleStep define then the executed time
	 * schedule
	 * 
	 * @param sinkStep
	 *            the sink step
	 * 
	 * @return maxlength
	 * 
	 */
	protected int computeSchedule(ModelStep sinkStep) {

		int d = computeScheduleStepsTo(sinkStep);

		return d;
	}

	/**
	 * 
	 */
	protected void createNewOrderedCollection() {
		orderedScheduleSteps = new TreeSet<ModelStep>(new ModelStepScheduleComparator());
	}

	/**
	 * reset to -1 modelstep schedule
	 */
	protected void razSchedule() {
		// reinit the schedule associated to all steps (-1 is put to all
		// elements)
		for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
			ModelStep s = (ModelStep) iterator.next();
			s.schedule = -1;
		}

		// clear the scheduled collection
		createNewOrderedCollection();

	}

	/**
	 * Compute scheduled operations to the step s passed in parameter s
	 * 
	 * @param s
	 * @return
	 */
	private int computeScheduleStepsTo(ModelStep s) {

		if (s.schedule != -1)
			return s.schedule;

		Set<ModelLink> l = getPrecedingLinksAssociatedTo(s);

		if (l.size() == 0) {
			// if no preceeding, the step schedule is 1

			int currentschedule = (s.schedule = 1);
			orderedScheduleSteps.add(s);
			return currentschedule;
		}

		int max = -1;
		// go to all the preceding elements, and compute the schedule order
		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
			ModelLink modelLink = (ModelLink) iterator.next();

			int prec = computeScheduleStepsTo(modelLink.getFrom().getStep());
			max = Math.max(max, prec);
		}

		int currentSchedule = (s.schedule = (max + 1));
		orderedScheduleSteps.add(s);
		return currentSchedule;
	}

	/**
	 * List requiered parameters associated to a step
	 * 
	 * @param step
	 * @return
	 */
	public Set<ModelParameter> getRequiredModelParameter(ModelStep step) {
		TreeSet<ModelParameter> l = new TreeSet<ModelParameter>(new ModelParameterComparator());
		ModelParameter[] parametersByRef = step.getAllParametersByRef();
		for (int i = 0; i < parametersByRef.length; i++) {
			ModelParameter modelParameter = parametersByRef[i];
			if (modelParameter.isIn() && !modelParameter.isOptional()) {
				l.add(modelParameter);
			}
		}
		return l;
	}

	public ModelStep findModelStepById(String id) {
		for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
			ModelStep ms = (ModelStep) iterator.next();
			if (id.equals(ms.getId()))
				return ms;
		}
		return null;
	}

	public ModelLink findLinkById(String id) {
		for (Iterator iterator = links.iterator(); iterator.hasNext();) {
			ModelLink link = (ModelLink) iterator.next();
			assert link != null;

			String linkid = link.getId();
			if (linkid == null) {
				if (id == null)
					return link;
			} else {
				assert linkid != null;
				if (linkid.equals(id))
					return link;
			}
		}
		return null;
	}

	/**
	 * find a parameter by id
	 * 
	 * @param id
	 * @return
	 */
	public AbstractParameter findParameterById(String id) {

		for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
			ModelStep ms = (ModelStep) iterator.next();
			AbstractParameter[] p = ms.getAllParametersByRef();
			for (int i = 0; i < p.length; i++) {
				AbstractParameter modelParameter = p[i];
				if (id.equals(modelParameter.getId())) {
					return modelParameter;
				}
			}
		}

		return null;
	}

	/**
	 * Find all terminalmodelstep
	 * 
	 * @param filterOutParameters
	 *            only terminalmodelstep that have out parameters
	 * @return
	 */
	public ArrayList<TerminalParameterModelStep> getTerminalModelStep(boolean filterOutParameters) {
		ArrayList<TerminalParameterModelStep> ret = new ArrayList<TerminalParameterModelStep>();
		for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
			ModelStep modelStep = (ModelStep) iterator.next();
			// all termminal parameters implements TerminalParameterModelStep
			if (modelStep instanceof TerminalParameterModelStep) {
				TerminalParameterModelStep ts = (TerminalParameterModelStep) modelStep;
				if (filterOutParameters) {
					if (!ts.isInput()) {
						ret.add((TerminalParameterModelStep) modelStep);
					}
				} else {
					ret.add((TerminalParameterModelStep) modelStep);
				}
			}
		}

		return ret;
	}

	public TerminalParameterModelStep getOutTerminalByName(String name) {
		ArrayList<TerminalParameterModelStep> terms = getTerminalModelStep(true);
		assert terms != null;
		for (TerminalParameterModelStep t : terms) {
			if (name.equals(t.getName())) {
				return t;
			}
		}
		return null;
	}

	public TerminalParameterModelStep getInTerminalByName(String name) {
		ArrayList<TerminalParameterModelStep> terms = getTerminalModelStep(false);
		assert terms != null;
		for (TerminalParameterModelStep t : terms) {
			if (name.equals(t.getName())) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Compute the schedule for all terminate elements
	 * 
	 * @return
	 */
	public Set<ModelStep> schedule() {

		razSchedule();

		// for each terminal parameter (sink terminal elements),
		// schedule the operation to permit executing steps

		// start from source and propagate to sinks

		for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
			ModelStep modelStep = (ModelStep) iterator.next();

			if (modelStep instanceof SinkSource) {
				SinkSource ss = (SinkSource) modelStep;
				if (ss.isSink()) {
					logger.debug("compute schedule for step :" + modelStep);
					computeSchedule(modelStep);
				}
			}
		}

		return orderedScheduleSteps;
	}

	/**
	 * Dump the model steps and the links of the model (useful for debugging)
	 * 
	 * @return
	 */
	public String dump() {
		StringBuffer sb = new StringBuffer();
		sb.append("Steps :\n");
		for (Iterator<ModelStep> iterator = steps.iterator(); iterator.hasNext();) {
			ModelStep s = iterator.next();
			sb.append(s.toString());
		}
		sb.append("Links :\n");
		for (Iterator<ModelLink> iterator = links.iterator(); iterator.hasNext();) {
			ModelLink l = iterator.next();
			sb.append(l.toString());
		}

		return sb.toString();
	}

	/**
	 * Visit on scheduled model, on scheduled steps
	 * 
	 * @param visitor
	 * @throws Exception
	 *             raise exception if the model is not scheduled or if the visit
	 *             raise exception
	 */
	public void visitBySchedule(ModelVisitor visitor) throws Exception {
		if (!isScheduled())
			throw new Exception("model is not scheduled");
		visit(orderedScheduleSteps, visitor);
	}

	/**
	 * Visit the model, on all steps
	 */
	public void visit(ModelVisitor visitor) {
		visit(steps, visitor);
	}

	/**
	 * Visit the model elements, ordered by the elements passed in parameters
	 * 
	 * @param elements
	 * @param visitor
	 */
	protected void visit(Set<ModelStep> elements, ModelVisitor visitor) {
		logger.debug("visit model with :" + visitor);
		if (visitor == null)
			return;

		for (Iterator<ModelStep> iterator = elements.iterator(); iterator.hasNext();) {

			ModelStep s = iterator.next();
			logger.debug("visit :" + s);
			visitor.visit(this, s);

			ModelParameter[] parametersByRef = s.getAllParametersByRef();
			for (int i = 0; i < parametersByRef.length; i++) {
				ModelParameter modelParameter = parametersByRef[i];
				logger.debug("visit :" + modelParameter);
				visitor.visit(this, modelParameter);
			}
		}

		for (Iterator<ModelLink> iterator = links.iterator(); iterator.hasNext();) {
			ModelLink l = iterator.next();
			logger.debug("visit :" + l);
			visitor.visit(this, l);
		}
	}

	/**
	 * 
	 * @param context
	 */
	public void hydrateContext(Map<String, Object> context) {
		if (context == null)
			return;

		for (ModelStep ms : steps) {
			if (ms instanceof IModelStepContextAware) {
				logger.debug("hydrate " + ms);
				((IModelStepContextAware) ms).defineContext(context);
			}
		}

	}

}
