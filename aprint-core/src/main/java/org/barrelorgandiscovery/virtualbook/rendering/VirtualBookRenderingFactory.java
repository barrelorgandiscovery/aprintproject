package org.barrelorgandiscovery.virtualbook.rendering;

import org.apache.log4j.Logger;

public class VirtualBookRenderingFactory {

	private static Logger logger = Logger
			.getLogger(VirtualBookRenderingFactory.class);

	private VirtualBookRenderingFactory() {

	}

	private static VirtualBookRendering[] list = new VirtualBookRendering[] {
			new VirtualBookRendering(), new PaperBookRendering(), new MusicBoxRendering() };

	public static VirtualBookRendering[] getRenderingList() {
		return list;
	}

	public static VirtualBookRendering createRenderingFromName(String name) {
		if (name != null) {
			VirtualBookRendering[] renderingList = getRenderingList();
			for (int i = 0; i < renderingList.length; i++) {
				VirtualBookRendering virtualBookRendering = renderingList[i];
				if (name.equalsIgnoreCase(virtualBookRendering.getName())) {
					logger.debug("return rendering "
							+ virtualBookRendering.getName());
					return virtualBookRendering;
				}
			}
		}
		logger.debug("return default rendering");
		return new VirtualBookRendering();

	}
}
