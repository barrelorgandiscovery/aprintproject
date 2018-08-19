package org.barrelorgandiscovery.gui.aedit.markers;

import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer;


public class BaseMarkerTool extends Tool {

	protected JEditableVirtualBookComponent editableComponent;
	
	
	public BaseMarkerTool(JEditableVirtualBookComponent editableComponent)
	{
		assert editableComponent != null;
		this.editableComponent = editableComponent;
	}
	
	@Override
	public void activated() {
		super.activated();
		
		VirtualBookComponentLayer ml = editableComponent.findLayerByClass(MarkerLayer.class);
		if (ml != null)
		{
			ml.setVisible(true);
			
			editableComponent.repaint();
		}
		
	}
	
	
}
