package org.barrelorgandiscovery.model.steps.midi;

import java.util.Map;

import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.type.JavaType;

public abstract class BaseMidiModelStep extends ModelStep {

	protected static final String CONFIG_SCALE = "scale";

	protected ModelParameter[] parameters = new ModelParameter[0];


	ModelValuedParameter fromNote;
	ModelValuedParameter toNote;
	
	public BaseMidiModelStep() {
		// config element
		this.fromNote = new ModelValuedParameter();
		fromNote.setName("fromnote");
		fromNote.setType(new JavaType(Integer.class));
		fromNote.setStep(this);
		fromNote.setValue(new Integer(0));

		this.toNote = new ModelValuedParameter();
		toNote.setName("tonote");
		toNote.setType(new JavaType(Integer.class));
		toNote.setStep(this);
		toNote.setValue(new Integer(127));
		
		this.configureParameters = 
				new ModelValuedParameter[] { fromNote, toNote };
	}

	@Override
	public ModelParameter[] getAllParametersByRef() {
		return parameters;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String getLabel() {
		String label = "";
		return "VB" + label;
	}

	protected String constructMidiCodeParameterName(int i) {
		return "mn" + i;
	}
	
	public abstract Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> values) throws Exception;
}