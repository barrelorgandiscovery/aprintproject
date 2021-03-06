package org.barrelorgandiscovery.extensionsng.scanner.wizardscan;

import java.awt.BorderLayout;
import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.Messages;
import org.barrelorgandiscovery.extensionsng.scanner.scan.JScanPanel;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.ITriggerFactory;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JOutputFolderChooserStep;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.tools.Disposable;

import com.github.sarxos.webcam.Webcam;

public class JScanStep extends BasePanelStep implements Disposable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9222279351912345017L;

	private static Logger logger = Logger.getLogger(JScanStep.class);
	
	private JScanPanel scanPanel;

	private JScanParameterStep previousScanParameterStep;
	private JOutputFolderChooserStep folderChooserStep;


	public JScanStep(Step parent, JOutputFolderChooserStep folderChooserStep) throws Exception {
		super("scan", parent); //$NON-NLS-1$
		assert parent instanceof JScanParameterStep;
		this.previousScanParameterStep = (JScanParameterStep) parent;
		assert folderChooserStep != null;
		this.folderChooserStep = folderChooserStep;
		this.initComponents();
	}

	protected void initComponents() throws Exception {

	}

	@Override
	public String getLabel() {
		return Messages.getString("JScanStep.1"); //$NON-NLS-1$
	}

	@Override
	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {

		if (scanPanel != null) {
			remove(scanPanel);
			scanPanel.dispose();
		}

		Webcam webcam = previousScanParameterStep.getOpenedWebCam();

		// get selected folder
		File folder = folderChooserStep.getFolder();
		assert folder != null;
		PerfoScanFolder psf = new PerfoScanFolder(folder);

		
		ITriggerFactory triggerFactory = previousScanParameterStep.getTriggerFactory();
		scanPanel = new JScanPanel(webcam, triggerFactory, psf);

		setLayout(new BorderLayout());
		add(scanPanel, BorderLayout.CENTER);

	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {
		// stop record if the user move
		scanPanel.stop();
		return null;
	}

	@Override
	public boolean isStepCompleted() {
		return scanPanel != null && !scanPanel.isStarted();
	}
	
	@Override
	public void dispose() {
		if (scanPanel != null) {
			try {
			scanPanel.stop();
			} catch(Throwable t) {
				logger.debug(t.getMessage(), t);
			}
			scanPanel.dispose();
		}
	}
	
}
