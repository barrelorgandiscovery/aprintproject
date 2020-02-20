package org.barrelorgandiscovery.model.steps.book;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelType;
import org.barrelorgandiscovery.model.enhanced.HoleWithScale;
import org.barrelorgandiscovery.model.type.CompositeType;
import org.barrelorgandiscovery.model.type.GenericSimpleType;
import org.barrelorgandiscovery.model.type.JavaType;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class VirtualBookDemultiplexer extends BaseVirtualBookModelStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4998348887558772555L;

	private static final String IN_PARAMETER_VIRTUALBOOK = "inVirtualbookOrHole";
	
	private static Logger logger = Logger.getLogger(VirtualBookDemultiplexer.class);

	private ModelParameter inVirtualBook;
	private Map<Integer, ModelParameter> outByTrack;

	public VirtualBookDemultiplexer() throws Exception {
		baseLabel = "Distributor";
		applyConfig();
	}

	@Override
	public void applyConfig() throws Exception {

		List<ModelParameter> parameters = new ArrayList<ModelParameter>();

		inVirtualBook = new ModelParameter();
		inVirtualBook.setName(IN_PARAMETER_VIRTUALBOOK);
		inVirtualBook.setType(new CompositeType(
				new ModelType[] { new GenericSimpleType(Collection.class, new Class[] { HoleWithScale.class }),
						new JavaType(VirtualBook.class) },
				"vborholes", "Virtual book or Holes"));

		inVirtualBook.setIn(true); // out
		inVirtualBook.setStep(this);

		inVirtualBook.setOptional(false);
		inVirtualBook.setLabel("Virtual Book");

		parameters.add(inVirtualBook);

		// depending on scale ,

		Scale configuredScale = (Scale) scaleForCreatedVirtualBook.getValue();

		if (configuredScale != null) {
			outByTrack = new HashMap<Integer, ModelParameter>();
			AbstractTrackDef[] tds = configuredScale.getTracksDefinition();
			for (int i = 0; i < tds.length; i++) {
				
				AbstractTrackDef abstractTrackDef = tds[i];

				ModelParameter p = new ModelParameter();
				p.setName("" + i); // track number
				p.setIn(false); // input parameters
				p.setType(new GenericSimpleType(Collection.class, new Class[] { HoleWithScale.class }));
				
				p.setLabel("Track" + " " + (i+1) + "-" + (abstractTrackDef != null ? " " + ScaleComponent.getTrackLibelle(abstractTrackDef) : ""));
				p.setOptional(true);
				p.setStep(this);

				outByTrack.put((Integer) i, p);

				parameters.add(p);
			}
		}

		this.parameters = parameters.toArray(new ModelParameter[0]);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelStep#execute(java.util.Map)
	 */
	@Override
	public Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> values) throws Exception {

		// get all holes from the different parameters
		// convert it to virtuel book with the scale definition

		if (inVirtualBook == null) {
			throw new Exception("implementation error, null inVirtualBook parameter");
		}
		logger.debug("start demux");
		log("start virtual book demultiplexer");

		Collection<Hole> holes = null;

		Object v = values.get(inVirtualBook);
		if (v instanceof VirtualBook) {
			holes = ((VirtualBook) v).getHolesCopy();
		} else {
			holes = (Collection<Hole>) v;
		}

		HashMap<Integer, Collection<HoleWithScale>> results = new HashMap<Integer, Collection<HoleWithScale>>();
		Scale configuredScale = (Scale) scaleForCreatedVirtualBook.getValue();

		assert holes != null;
		for (Hole h : holes) {
			int track = h.getTrack();
			Collection<HoleWithScale> t = results.get((Integer) track);
			if (t == null) {
				t = new ArrayList<HoleWithScale>();
				results.put((Integer) track, t);
			}
			t.add(new HoleWithScale(h, configuredScale));
		}

		HashMap<AbstractParameter, Object> returnedValues = new HashMap<AbstractParameter, Object>();

		for (Entry<Integer, Collection<HoleWithScale>> e : results.entrySet()) {
			returnedValues.put(outByTrack.get(e.getKey()), e.getValue());
		}
		logger.debug("end of demux");
		log("end of virtual book demultiplexer");

		return returnedValues;
	}

}
