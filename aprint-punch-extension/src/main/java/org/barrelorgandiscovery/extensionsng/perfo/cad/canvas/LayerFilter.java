package org.barrelorgandiscovery.extensionsng.perfo.cad.canvas;

public interface LayerFilter {

	// if returned tru, the layer is drawn
	boolean drawLayerObject(String layerName);
	
}
