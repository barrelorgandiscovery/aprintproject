package org.barrelorgandiscovery.model.steps.midi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelParameterHelper;
import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.type.GenericSimpleType;
import org.barrelorgandiscovery.model.type.JavaType;
import org.barrelorgandiscovery.timed.ITimedLength;
import org.barrelorgandiscovery.timed.ITimedStamped;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiAdvancedEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiNote;

public class MidiDemultiplexer extends BaseMidiModelStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9157425399407615538L;

	private static final String IN_PARAMETER_MIDI = "midiin";

	private ModelValuedParameter filterChannel;
	
	private static Logger logger = Logger.getLogger(MidiDemultiplexer.class);
	
	public MidiDemultiplexer() throws Exception {
		super();
		
		this.filterChannel = new ModelValuedParameter();
		filterChannel.setName("channel_filter");
		filterChannel.setType(new JavaType(String.class));
		filterChannel.setStep(this);
		filterChannel.setValue("");
		
		this.configureParameters = 
				new ModelValuedParameter[] { fromNote, toNote , filterChannel};
		
		applyConfig();
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelStep#applyConfigOnParameters()
	 */
	@Override
	public void applyConfig() throws Exception {

		List<ModelParameter> parameters = new ArrayList<ModelParameter>();

		ModelParameter outVirtualBook = new ModelParameter();
		outVirtualBook.setName(IN_PARAMETER_MIDI);
		outVirtualBook.setType(new GenericSimpleType(Collection.class, new Class[] { MidiAdvancedEvent.class }));
		outVirtualBook.setIn(true);
		outVirtualBook.setStep(this);

		outVirtualBook.setOptional(false);
		outVirtualBook.setLabel("Midi In");

		parameters.add(outVirtualBook);

		for (int i = 0; i <= 127; i++) {

			if (fromNote.getValue() != null) {
				if (i < (Integer) fromNote.getValue())
					continue;
			}

			if (toNote.getValue() != null) {
				if (i > (Integer) toNote.getValue())
					continue;
			}

			ModelParameter p = new ModelParameter();
			p.setName(constructMidiCodeParameterName(i));
			p.setIn(false); // input parameters
			p.setType(new GenericSimpleType(Collection.class, new Class[] { ITimedLength.class }));

			p.setLabel(MidiHelper.localizedMidiLibelle(i) + "(" + i + ")");
			p.setOptional(true);
			p.setStep(this);

			parameters.add(p);

		}

		this.parameters = parameters.toArray(new ModelParameter[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.model.steps.midi.BaseMidiModelStep#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Midi Distributor";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.model.steps.midi.BaseMidiModelStep#execute(java.
	 * util.Map)
	 */
	@Override
	public Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> values) throws Exception {

		ModelParameterHelper ps = ModelParameterHelper.wrap(values);
		
		Collection<ITimedStamped> events = ps.getValue(IN_PARAMETER_MIDI);
		
		String midiFilterValue = (String)filterChannel.getValue();
		Set<Integer> filtered = null;
		if (midiFilterValue != null && (!midiFilterValue.trim().isEmpty()) ) {
			log("using midifilter :" + midiFilterValue);
			// read filter values
			String[] splittedValues = midiFilterValue.split(","); 
			if (splittedValues != null) {
				for (String v : splittedValues) {
					if (v != null && !v.trim().isEmpty()) {
						Integer filteredChannel = Integer.parseInt(v.trim());
						if (filtered == null) {
							filtered = new HashSet<Integer>();
						}
						filtered.add(filteredChannel);
					}
				}
			}
		}
		

		Map<AbstractParameter, Object> ret = new HashMap<AbstractParameter, Object>();

		if (events == null) {
			return ret;
		}

		Map<Integer, List<MidiNote>> byNote = new HashMap<Integer, List<MidiNote>>();

		for (ITimedStamped s : events) {
			if (s instanceof MidiNote) {
				MidiNote mn = (MidiNote) s;
				
				if (filtered != null && !filtered.contains( mn.getChannel())   ) {
					continue;
				}
				
				List<MidiNote> l = byNote.get(mn.getMidiNote());
				if (l == null) {
					l = new ArrayList<>();
				}
				l.add(mn);
				byNote.put(mn.getMidiNote(), l);
			} 
		}

		for (Integer i : byNote.keySet()) {
			ModelParameter p = getParameterByName(constructMidiCodeParameterName(i));
			if (p == null) {
				logger.debug("midi note "+ i + " has no parameter, and will not be mapped");
				continue;
			}
			assert p != null;
			ret.put(p, byNote.get(i));
		}

		return ret;
	}

}
