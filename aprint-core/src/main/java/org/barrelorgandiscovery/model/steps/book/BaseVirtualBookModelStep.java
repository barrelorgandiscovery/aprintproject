package org.barrelorgandiscovery.model.steps.book;

import java.util.Map;

import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelStepWithConsole;
import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.type.JavaType;
import org.barrelorgandiscovery.scale.Scale;

public abstract class BaseVirtualBookModelStep extends ModelStepWithConsole {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3683394740391630146L;

	protected static final String CONFIG_SCALE = "scale";

	protected ModelParameter[] parameters = new ModelParameter[0];

	ModelValuedParameter scaleForCreatedVirtualBook;

	public BaseVirtualBookModelStep() {
		// config element
		this.scaleForCreatedVirtualBook = new ModelValuedParameter();
		scaleForCreatedVirtualBook.setName(CONFIG_SCALE);
		scaleForCreatedVirtualBook.setType(new JavaType(Scale.class));
		scaleForCreatedVirtualBook.setStep(this);

		this.configureParameters = new ModelValuedParameter[] { scaleForCreatedVirtualBook };
	}

	@Override
	public ModelParameter[] getAllParametersByRef() {
		return parameters;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	protected String baseLabel = "VB";
	
	@Override
	public String getLabel() {
		String label = "";
		if (scaleForCreatedVirtualBook != null) {
			Scale scale = (Scale) scaleForCreatedVirtualBook.getValue();
			if (scale != null) {
				label = "-" + scale.getName();
			}
		}
		return baseLabel + label;
	}

	public abstract Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> values) throws Exception;
}