package org.barrelorgandiscovery.recognition.gui.books.steps;

import org.barrelorgandiscovery.recognition.gui.books.steps.models.ModelResourceMarker;
import org.barrelorgandiscovery.tools.ImageTools;

public class ModelFactory {

	/**
	 * create the models
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Model[] createModels() throws Exception {
		return new Model[] { 
				loadResourceModel("classifier1")};
	}

	/**
	 * create a resource model
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private static Model loadResourceModel(String name) throws Exception {

		Model m = new Model(name, name, ImageTools.loadImage(ModelResourceMarker.class.getResource(name + ".jpg")),
				ModelResourceMarker.class.getResource(name + ".model"));

		return m;
	}

}
