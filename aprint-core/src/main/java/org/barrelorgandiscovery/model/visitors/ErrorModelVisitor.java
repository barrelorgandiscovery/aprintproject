package org.barrelorgandiscovery.model.visitors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.Model;
import org.barrelorgandiscovery.model.ModelLink;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.ModelVisitor;

/**
 * Visitor for checking model configuration problems
 * 
 * @author use
 * 
 */
public class ErrorModelVisitor extends ModelVisitor {

	public enum Target {
		STEP, LINK, PARAMETER
	}

	/**
	 * Bag for errors
	 * 
	 * @author use
	 * 
	 */
	public static class Error {

		public Error(String id, Target target, String errorCode) {
			this.id = id;
			this.target = target;
			this.errorCode = errorCode;

		}

		public String id;
		public Target target;
		public String errorCode;

	}

	private List<Error> errors = new ArrayList<Error>();

	public ErrorModelVisitor() {

	}

	@Override
	public void visit(Model model, ModelParameter parameter) {

	}

	@Override
	public void visit(Model model, ModelStep step) {

		// checking the required parameters for the step
		ModelParameter[] p = step.getAllParametersByRef();
		if (p != null) {
			for (int i = 0; i < p.length; i++) {
				ModelParameter modelParameter = p[i];
				if (modelParameter.isIn() && !modelParameter.isOptional()) {
					Set<ModelLink> pl = model
							.getPrecedingLinksAssociatedTo(step);

					boolean foundLink = false;
					for (Iterator iterator = pl.iterator(); iterator.hasNext();) {
						ModelLink modelLink = (ModelLink) iterator.next();
						if (modelLink.getTo() == modelParameter) {
							// parameter has a link connected to
							if (foundLink) {
								// already connected
								pushError(modelParameter.getId(),
										Target.PARAMETER,
										"MULTIPLE_CONNECTIONS_NOT_ALLOWED");
							} else {
								foundLink = true;

								// check links parameter type

								if (modelLink.getFrom().getType() != modelLink
										.getTo().getType()) {
									pushError(modelLink.getId(), Target.LINK,
											"TYPE_MISMATCH");
								}

							}

						}
					}
					if (!foundLink) {
						pushError(step.getId(), Target.STEP,
								"REQUIRED_PARAMETER_NOT_CONNECTED");
					}

				}
			}
		}

	}

	protected void pushError(String id, Target target, String errorCode) {
		errors.add(new Error(id, target, errorCode));
	}

	@Override
	public void visit(Model model, ModelLink link) {

	}

	/**
	 * After visiting, return the model errors
	 * 
	 * @return
	 */
	public List<Error> getErrors() {
		return errors;
	}

}
