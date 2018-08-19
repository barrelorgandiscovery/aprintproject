package org.barrelorgandiscovery.model.steps.midi;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.type.GenericSimpleType;
import org.barrelorgandiscovery.model.type.JavaType;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiAdvancedEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;

public class MidiFileInput extends ModelStep  {

	
	private ModelParameter midifileinput;
	private ModelParameter outputEventCollection;
	
	/**
	 * 
	 */
	public MidiFileInput() {
		super();
		
		ModelParameter i = new ModelParameter();
		i.setIn(true);
		i.setType(new JavaType(File.class));
		i.setLabel("Midi File Reader");
		i.setName("inmidifile");
		i.setOptional(false);
		i.setStep(this);
		
		this.midifileinput = i;
		
		
		ModelParameter o = new ModelParameter();
		o.setIn(false);
		o.setType(new GenericSimpleType(Collection.class, new Class[]{MidiAdvancedEvent.class}));
		o.setName("events");
		o.setLabel("Events");
		o.setStep(this);
		this.outputEventCollection = o;
		
		setLabel("Midi File Reading");
	}
	

	@Override
	public ModelParameter[] getAllParametersByRef() {
		return new ModelParameter[]{midifileinput, outputEventCollection};
	}
	

	@Override
	public String getName() {
		return "midifileinput";
	}

	@Override
	public void applyConfig() throws Exception {
		
	}

	@Override
	public Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> inputValues) throws Exception {

		if (inputValues == null)
			throw new NullPointerException("inputValues is null");
		
		Object v = inputValues.get(midifileinput);
		assert v instanceof File;
		
		MidiFile r = MidiFileIO.read((File)v);
		
		assert r instanceof Collection;
		
		Map<AbstractParameter, Object> result = new HashMap<>();
		result.put(outputEventCollection, r);
		return result;
	}

}
