package org.barrelorgandiscovery.model.steps.book;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.enhanced.HoleWithScale;
import org.barrelorgandiscovery.model.type.GenericSimpleType;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.timed.ITimedLength;
import org.barrelorgandiscovery.virtualbook.Hole;

/**
 * This step create a VirtualBook Object from holes
 * 
 * @author pfreydiere
 * 
 */
public class VirtualBookMultiplexer extends BaseVirtualBookModelStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2285504875157444234L;

	static final String OUT_PARAMETER_VIRTUALBOOK = "outVirtualBook";

	public VirtualBookMultiplexer() throws Exception {
		baseLabel = "Concentrator";
		// add input parameter
		applyConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelStep#execute(java.util.Map)
	 */
	@Override
	public Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> values) throws Exception {

		HashMap<AbstractParameter, Object> ret = new HashMap<AbstractParameter, Object>();

		ModelParameter[] ps = getInputParametersByRef();
		assert ps != null;

		List<Hole> l = new ArrayList<Hole>();

		for (ModelParameter p : ps) {
			int track = Integer.parseInt(p.getName());

			Collection<ITimedLength> v = (Collection<ITimedLength>) values.get(p);
			if (v != null) {
				for (ITimedLength t : v) {
					Hole h = new Hole(track, t.getTimestamp(), t.getTimeLength());
					l.add(new HoleWithScale(h, (Scale) this.scaleForCreatedVirtualBook.getValue()));
				}
			}
		}

		ModelParameter op = getParameterByName(OUT_PARAMETER_VIRTUALBOOK);
		ret.put(op, l);
		return ret;
	}

	@Override
	public void applyConfig() throws Exception {

		List<ModelParameter> parameters = new ArrayList<ModelParameter>();

		ModelParameter outVirtualBook = new ModelParameter();
		outVirtualBook.setName(OUT_PARAMETER_VIRTUALBOOK);
		outVirtualBook.setType(new GenericSimpleType(Collection.class, new Class[] { HoleWithScale.class }));
		outVirtualBook.setIn(false); // out
		outVirtualBook.setStep(this);

		outVirtualBook.setOptional(false);
		outVirtualBook.setLabel("Virtual Book");

		parameters.add(outVirtualBook);

		// depending on scale ,

		Scale configuredScale = (Scale) scaleForCreatedVirtualBook.getValue();

		if (configuredScale != null) {
			AbstractTrackDef[] tds = configuredScale.getTracksDefinition();
			for (int i = 0; i < tds.length; i++) {
				AbstractTrackDef abstractTrackDef = tds[i];

				ModelParameter p = new ModelParameter();
				p.setName("" + i);
				p.setIn(true); // input parameters
				p.setType(new GenericSimpleType(Collection.class, new Class[] { ITimedLength.class }));

				p.setLabel("Track " + (i+1) + (abstractTrackDef != null ? " " + ScaleComponent.getTrackLibelle(abstractTrackDef) : ""));
				p.setOptional(true);
				p.setStep(this);

				parameters.add(p);
			}
		}

		this.parameters = parameters.toArray(new ModelParameter[0]);

	}

}
