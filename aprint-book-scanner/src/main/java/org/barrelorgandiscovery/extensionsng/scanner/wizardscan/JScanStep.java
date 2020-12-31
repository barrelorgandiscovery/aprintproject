package org.barrelorgandiscovery.extensionsng.scanner.wizardscan;

import java.awt.BorderLayout;
import java.io.File;
import java.io.Serializable;

import org.barrelorgandiscovery.bookimage.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.Messages;
import org.barrelorgandiscovery.extensionsng.scanner.scan.JScanPanel;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JOutputFolderChooserStep;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;

import com.github.sarxos.webcam.Webcam;

public class JScanStep extends BasePanelStep {

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

		scanPanel = new JScanPanel(webcam, previousScanParameterStep.getTriggerFactory(), psf);

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
}
