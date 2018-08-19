package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;

/**
 * this interface inform a component can be used for been aware of tool changes
 * @author pfreydiere
 *
 */
public interface CurrentToolChangedAware {

	public void addCurrentToolChangedListener(CurrentToolChanged listener);

	public void removeCurrentToolChangedListener(CurrentToolChanged listener);

}