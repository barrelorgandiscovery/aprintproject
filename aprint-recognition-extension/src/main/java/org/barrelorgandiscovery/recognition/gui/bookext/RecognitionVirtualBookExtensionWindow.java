package org.barrelorgandiscovery.recognition.gui.bookext;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.IStatusBarFeedBackTransactional;
import org.barrelorgandiscovery.gui.aprintng.IStatusBarFeedback;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameToolRegister;
import org.barrelorgandiscovery.gui.aprintng.helper.BaseVirtualBookExtension;
import org.barrelorgandiscovery.images.books.tools.ITiledImage;
import org.barrelorgandiscovery.ui.tools.ToolWindowTools;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

public class RecognitionVirtualBookExtensionWindow extends BaseVirtualBookExtension
		implements VirtualBookFrameToolRegister, IRecognitionToolWindowCommands {

	JRecognitionVirtualBookPanel recognitionPanel;

	public RecognitionVirtualBookExtensionWindow() throws Exception {
		super();
		recognitionPanel = new JRecognitionVirtualBookPanel();
		recognitionPanel.setPreferredSize(new Dimension(300, 900));
	}

	@Override
	protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints) throws Exception {
		super.setupExtensionPoint(initExtensionPoints);
		initExtensionPoints.add(new SimpleExtensionPoint(VirtualBookFrameToolRegister.class, this));
		initExtensionPoints.add(new SimpleExtensionPoint(IRecognitionToolWindowCommands.class, this));
	}

	@Override
	public void informVirtualBookFrame(APrintNGVirtualBookFrame frame) {
		super.informVirtualBookFrame(frame);
		JVirtualBookScrollableComponent pianoroll = frame.getPianoRoll();
		recognitionPanel.setVirtualBookComponent(pianoroll);
	}


	@Override
	public void informStatusBarTransactional(IStatusBarFeedBackTransactional statusbar) {
		recognitionPanel.setStatusBar(statusbar);
	}

	@Override
	public void setTiledImage(ITiledImage tiledImage) {
		// forward
		recognitionPanel.setTiledImage(tiledImage);
	}

	@Override
	public void addLayers(JVirtualBookScrollableComponent c) {
		c.addLayer(recognitionPanel.backgroundBook);

		c.addLayer(recognitionPanel.recognitionDisplay);

		c.addLayer(recognitionPanel.bookRegionDisplay);

		c.addLayer(recognitionPanel.holeRegionDisplay);

		c.addLayer(recognitionPanel.bookNotesOverlay);
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

		JScrollPane jscrollPane = new JScrollPane(recognitionPanel);
		jscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// Register a Tool.
		ToolWindow registeredToolWindow = manager.registerToolWindow("Book Recognition", // Id //$NON-NLS-1$
				"Book recognition", // Title //$NON-NLS-1$
				null, // Icon
				jscrollPane, // Component
				ToolWindowAnchor.LEFT); // Anchor

		ToolWindowTools.defineProperties(registeredToolWindow);

		// change width
		DockedTypeDescriptor desc = (DockedTypeDescriptor) registeredToolWindow
				.getTypeDescriptor(ToolWindowType.DOCKED);
		desc.setDockLength(550);
	}

}
