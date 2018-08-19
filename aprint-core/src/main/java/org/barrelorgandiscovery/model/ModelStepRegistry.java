package org.barrelorgandiscovery.model;

import java.util.Iterator;
import java.util.List;

import org.barrelorgandiscovery.tools.SerializeTools;

/**
 * Get all the available model steps
 * 
 * @author pfreydiere
 * 
 */
public abstract class ModelStepRegistry {

	public abstract List<ModelStep> getRegisteredModelStepList() throws Exception;

	public ModelStep getModelStepByName(String name) throws Exception {
		if (name == null)
			return null;

		List<ModelStep> list = getRegisteredModelStepList();

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			ModelStep ms = (ModelStep) iterator.next();
			if (name.equalsIgnoreCase(ms.getName())) {

				return (ModelStep) SerializeTools.deepClone(ms);
			}
		}

		return null;
	}

}
