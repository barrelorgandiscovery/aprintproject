package org.barrelorgandiscovery.gui.ascale;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering;

public class VirtualBookRenderingDisplay {

	private VirtualBookRendering v;

	public VirtualBookRenderingDisplay(VirtualBookRendering v) {
		this.v = v;
	}

	public VirtualBookRendering getRendering() {
		return v;
	}

	@Override
	public String toString() {

		return Messages.getString("Rendering." + v.getName().toUpperCase());
	}

}
