package org.barrelorgandiscovery.recognition.gui.interactivecanvas;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Layer containing objects
 * 
 * @author pfreydiere
 */
public abstract class JLayer {

	private static Logger logger = Logger.getLogger(JLayer.class);
	
	/**
	 * get the name of the layer
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * draw layer
	 * @param g2d
	 */
	public abstract void drawLayer(Graphics2D g2d);
	
	
	private java.util.Vector<LayerChangedListener> listeners = new Vector<LayerChangedListener>();
	
	public void addLayerChangedListener(LayerChangedListener listener)
	{
		if (listener != null)
			listeners.add(listener);
	}
	
	public void removeLayerChangedListener(LayerChangedListener listener)
	{
		listeners.remove(listener);
	}
	
	
	
	/**
	 * this method call the listeners contentChanged method
	 */
	protected void fireLayerContentChanged()
	{
		for (LayerChangedListener layerChanged : listeners) {
			try {
				layerChanged.layerContentChanged();
			} catch(Exception ex)
			{
				logger.error("error in layerchanged listener " + layerChanged, ex);
			}
		}
	}
	
	protected void fireLayerSelectionChanged()
	{
		for (LayerChangedListener layerChanged : listeners) {
			try {
				layerChanged.layerSelectionChanged();
			} catch(Exception ex)
			{
				logger.error("error in layerchanged listener "+layerChanged);
			}
		}
	}
	
	public abstract Rectangle2D getExtent();
	
}
