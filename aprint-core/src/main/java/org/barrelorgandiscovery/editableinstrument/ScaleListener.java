package org.barrelorgandiscovery.editableinstrument;

import org.barrelorgandiscovery.scale.Scale;

public interface ScaleListener {

	void ScaleChanged(Scale oldScale, Scale newScale);

}
