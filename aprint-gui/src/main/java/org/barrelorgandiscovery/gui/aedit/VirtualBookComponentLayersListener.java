package org.barrelorgandiscovery.gui.aedit;

/**
 * Interface for signaling changes in the layer composition
 * 
 * @author use
 * @since 2012.6
 * 
 */
public interface VirtualBookComponentLayersListener {

	/**
	 * Signal a layer has been added
	 * @param layer
	 */
	public void layerAdded(VirtualBookComponentLayer layer);

	/**
	 * Signal a layer has been removed
	 * @param layer
	 */
	public void layerRemoved(VirtualBookComponentLayer layer);

	/**
	 * Signal the layer collection has changed
	 */
	public void layersChanged();

}
