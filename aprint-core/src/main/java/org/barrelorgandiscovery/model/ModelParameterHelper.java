package org.barrelorgandiscovery.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ModelParameterHelper {

	private static final Logger logger = Logger.getLogger(ModelParameterHelper.class);

	Map<AbstractParameter, Object> inner;
	Map<String, AbstractParameter> byName;

	public static ModelParameterHelper wrap(Map<AbstractParameter, Object> wrapped) throws Exception {
		assert wrapped != null;

		ModelParameterHelper mp = new ModelParameterHelper();
		mp.inner = wrapped;

		Map<String, AbstractParameter> h = new HashMap<String, AbstractParameter>();
		for (AbstractParameter p : wrapped.keySet()) {
			h.put(p.getName(), p);
		}
		mp.byName = h;

		return mp;
	}

	/**
	 * get a parameter value
	 * 
	 * @param parameterName
	 * @return
	 * @throws Exception
	 */
	public <T> T getValue(String parameterName) throws Exception {

		if (!byName.containsKey(parameterName)) {
			throw new Exception("parameter " + parameterName + " not found in " + byName.keySet());
		}

		AbstractParameter p = byName.get(parameterName);
		assert p != null;

		Object v = inner.get(p);

		if (v == null) {
			logger.debug("value associated to parameter " + parameterName + " is null");
			return (T) v;
		}

		return (T) v;
	}

}
