package org.barrelorgandiscovery.recognition.gui.bookext;

import java.util.List;

import javax.swing.JToolBar;

import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameToolRegister;
import org.barrelorgandiscovery.gui.aprintng.helper.BaseVirtualBookExtension;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

public class RecognitionVirtualBookExtensionWindow extends BaseVirtualBookExtension
		implements VirtualBookFrameToolRegister {

	JRecognitionVirtualBookPanel recognitionPanel;

	public RecognitionVirtualBookExtensionWindow() throws Exception {
		super();
		recognitionPanel = new JRecognitionVirtualBookPanel();
	}

	@Override
	protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints) throws Exception {
		super.setupExtensionPoint(initExtensionPoints);
		initExtensionPoints.add(new SimpleExtensionPoint(VirtualBookFrameToolRegister.class, this));
	}

	@Override
	public void informVirtualBookFrame(APrintNGVirtualBookFrame frame) {
		super.informVirtualBookFrame(frame);
		JVirtualBookScrollableComponent pianoroll = frame.getPianoRoll();
		recognitionPanel.setVirtualBookComponent(pianoroll);
	}

	@Override
	public void addLayers(JVirtualBookScrollableComponent c) {
		c.addLayer(recognitionPanel.backgroundBook);

		c.addLayer(recognitionPanel.recognitionDisplay);

		c.addLayer(recognitionPanel.bookRegionDisplay);

		c.addLayer(recognitionPanel.holeRegionDisplay);
	}

	@Override
	public JToolBar[] addToolBars() {
		return null;
	}

	@Override
	public String getName() {
		return "Recognition window extension";
	}

	@Override
	public void registerToolWindow(MyDoggyToolWindowManager manager) {
		// Register a Tool.
		manager.registerToolWindow("Book Recognition", // Id //$NON-NLS-1$
				"Book recognition", // Title //$NON-NLS-1$
				null, // Icon
				recognitionPanel, // Component
				ToolWindowAnchor.LEFT); // Anchor

	}

}
