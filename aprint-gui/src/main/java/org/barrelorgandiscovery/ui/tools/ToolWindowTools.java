package org.barrelorgandiscovery.ui.tools;

import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowType;

public class ToolWindowTools {

	/**
	 * define common properties for toolwindow in APrint
	 * 
	 * @param tw
	 */
	public static void defineProperties(ToolWindow tw) {

		assert tw != null;

		tw.setType(ToolWindowType.DOCKED);
		tw.setLockedOnAnchor(true);
		DockedTypeDescriptor descriptor = tw
				.getTypeDescriptor(DockedTypeDescriptor.class);
		descriptor.setTitleBarButtonsVisible(false);
		descriptor.setTitleBarVisible(false);
	}

}
