package org.barrelorgandiscovery.extensionsng.perfo.ng;

import java.io.File;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.extensionsng.perfo.gui.PunchLayer;
import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;
import org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard.JPunchWizard;
import org.barrelorgandiscovery.gui.aedit.IVirtualBookChangedListener;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentVirtualBookExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.LayersExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InformVirtualBookFrameExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InitNGExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameToolRegister;
import org.barrelorgandiscovery.issues.IssueLayer;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.ui.tools.ToolWindowTools;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;


/**
 * Extension de perçage pour la machine à percer de gérard
 * 
 * @author Freydiere Patrice
 * 
 */
public class PerfoExtensionMachineVirtualBook
		implements IExtension, InitNGExtensionPoint, LayersExtensionPoint, InformCurrentVirtualBookExtensionPoint,
		VirtualBookFrameToolRegister, InformVirtualBookFrameExtensionPoint, Disposable {

	private static Logger logger = Logger.getLogger(PerfoExtensionMachineVirtualBook.class);

	// points d'extension ...

	private IssueLayer issuesPunchLayer = null;
	private PunchLayer resultPunchLayer = null;

	private JPunchWizard punchPanel = null;

	public PerfoExtensionMachineVirtualBook() throws Exception {

		this.issuesPunchLayer = new IssueLayer();
		issuesPunchLayer.setLayerName(Messages.getString("PerfoExtensionMachineVirtualBook.0")); //$NON-NLS-1$

		PunchLayer pl = new PunchLayer();
		pl.setOrigin(PunchLayer.ORIGIN_CENTER);

		this.resultPunchLayer = pl;
		resultPunchLayer.setLayerName(Messages.getString("PerfoExtensionMachineVirtualBook.1")); //$NON-NLS-1$

	}

	public ExtensionPoint[] getExtensionPoints() {

		try {
			return new ExtensionPoint[] {
					new SimpleExtensionPoint(InitNGExtensionPoint.class, PerfoExtensionMachineVirtualBook.this),
					new SimpleExtensionPoint(LayersExtensionPoint.class, PerfoExtensionMachineVirtualBook.this),
					new SimpleExtensionPoint(InformCurrentVirtualBookExtensionPoint.class,
							PerfoExtensionMachineVirtualBook.this),
					new SimpleExtensionPoint(InformVirtualBookFrameExtensionPoint.class,
							PerfoExtensionMachineVirtualBook.this),
					new SimpleExtensionPoint(VirtualBookFrameToolRegister.class, PerfoExtensionMachineVirtualBook.this)

			};
		} catch (Exception ex) {
			logger.error("error in declaring the extension points, " + ex.getMessage()); //$NON-NLS-1$
			return new ExtensionPoint[0];
		}
	}

	public String getName() {
		return Messages.getString("PerfoExtensionMachineVirtualBook.2");  //$NON-NLS-1$
	}

	// cycle de vie

	private APrintNG aprintref;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aprintng.extensionspoints.
	 * InitNGExtensionPoint#init(org.barrelorgandiscovery.gui.aprintng.APrintNG)
	 */
	public void init(APrintNG f) {

		try {
			assert f != null;

			logger.debug("Initialize punch extension"); //$NON-NLS-1$
			this.aprintref = f;

			// loading preferences associated to the application ...

			logger.debug("loading properties ... "); //$NON-NLS-1$
			APrintProperties properties = aprintref.getProperties();
			File aprintFolder = properties.getAprintFolder();
			if (!aprintFolder.exists()) {
				logger.debug("aprintfolder doesn't exist, create it .. "); //$NON-NLS-1$
				aprintFolder.mkdir();
			}

		} catch (Exception ex) {
			logger.error("error in init :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.gui.aprint.extensionspoints.LayersExtensionPoint
	 * #addLayers(org.barrelorgandiscovery.gui.aedit.
	 * JVirtualBookScrollableComponent)
	 */
	public void addLayers(JVirtualBookScrollableComponent c) {
		c.addLayer(issuesPunchLayer);
		c.addLayer(resultPunchLayer);
	}

	public void removeLayers(JVirtualBookScrollableComponent c) {
		c.removeLayer(issuesPunchLayer);
		c.removeLayer(resultPunchLayer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aprint.extensionspoints.
	 * InformCurrentVirtualBookExtensionPoint#informCurrentVirtualBook(org.
	 * barrelorgandiscovery.virtualbook.VirtualBook)
	 */
	public void informCurrentVirtualBook(VirtualBook vb) {
		try {
			if (punchPanel != null) {
				logger.debug("inform virtual book :" + vb); //$NON-NLS-1$
				this.punchPanel.setVirtualBook(vb);
			}
		} catch (Exception ex) {
			logger.error("fail to report vitual book object :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	private IPrefsStorage cps = null;

	// reference to the book windows
	private APrintNGVirtualBookFrame frame;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aprintng.extensionspoints.
	 * InformVirtualBookFrameExtensionPoint#informVirtualBookFrame(org.
	 * barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame)
	 */
	public void informVirtualBookFrame(APrintNGVirtualBookFrame frame) {
		this.frame = frame;
		try {
			punchPanel = new JPunchWizard(resultPunchLayer, issuesPunchLayer, aprintref.getAsyncJobs(),
					aprintref.getPrefsStorage(getName()), frame.getPianoRoll());

			// register for changes on virtual book changes
			JEditableVirtualBookComponent pianoRoll = (JEditableVirtualBookComponent) frame.getPianoRoll();
			pianoRoll.addVirtualBookChangedListener(new IVirtualBookChangedListener() {
				@Override
				public void virtualBookChanged(VirtualBook newVirtualBook) {
					try {
						punchPanel.setVirtualBook(newVirtualBook);
					} catch (Exception ex) {
						logger.error("fail to set virtualbook :" + ex.getMessage(), ex); //$NON-NLS-1$
					}
				}
			});

			punchPanel.setScrollableVirtualBook(pianoRoll);

		} catch (Exception ex) {
			logger.error("fail to start extension :" + ex.getMessage(), ex); //$NON-NLS-1$
		}

	}

	public void registerToolWindow(MyDoggyToolWindowManager manager) {

		// Register a Tool.
		ToolWindow tw = manager.registerToolWindow("GCode Perfo Windows", // Id //$NON-NLS-1$
				"GCode Perfo Window", // Title //$NON-NLS-1$
				null, // Icon
				punchPanel, // Component
				ToolWindowAnchor.RIGHT); // Anchor

		ToolWindowTools.defineProperties(tw);
		// change width
		DockedTypeDescriptor desc = (DockedTypeDescriptor)tw.getTypeDescriptor(ToolWindowType.DOCKED);
		desc.setDockLength(550);
	}

	@Override
	public void dispose() {
		if (punchPanel != null) {
			punchPanel.dispose();
			punchPanel = null;
		}
	}

}
