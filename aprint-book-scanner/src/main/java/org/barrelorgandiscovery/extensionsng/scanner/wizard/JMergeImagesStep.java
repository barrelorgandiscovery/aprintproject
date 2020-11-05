package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.BorderLayout;
import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.scanner.IFamilyImageSeeker;
import org.barrelorgandiscovery.extensionsng.scanner.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.merge.JScannerMergePanel;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

public class JMergeImagesStep extends BasePanelStep {

	private static Logger logger = Logger.getLogger(JMergeImagesStep.class);
	
	private JScannerMergePanel mergePanel;

	private IFamilyImageSeeker perfoScanFolder;

	private IPrefsStorage ps;

	public JMergeImagesStep(Step parent, IPrefsStorage prefsStorage) throws Exception {
		super("mergeimagestep", parent);
		this.ps = prefsStorage;
		initComponents();
	}

	protected void initComponents() throws Exception {
		setLayout(new BorderLayout());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.wizard.Step#activate(java.io.Serializable,
	 * org.barrelorgandiscovery.gui.wizard.WizardStates,
	 * org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener)
	 */
	@Override
	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {

		File passedFolder = allStepsStates.getPreviousStateImplementing(this, File.class);

		assert passedFolder != null;

		if (mergePanel != null) {
			mergePanel.dispose();
			remove(mergePanel);
		}

		File scanfolder = passedFolder;
		PerfoScanFolder perfoScanFolder = new PerfoScanFolder(scanfolder);

		mergePanel = new JScannerMergePanel(perfoScanFolder, ps);
		int firstIndex = perfoScanFolder.getFirstImageIndex();
		if (firstIndex != -1) {
			logger.warn("no images in the folder");
			mergePanel.setCurrentImage(firstIndex);
		}
		add(mergePanel, BorderLayout.CENTER);
	}

	@Override
	public String getLabel() {
		return "Construct Image from acquisition";
	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {
		// no saved state
		return null;
	}

	@Override
	public boolean isStepCompleted() {
		return false;
	}
}
