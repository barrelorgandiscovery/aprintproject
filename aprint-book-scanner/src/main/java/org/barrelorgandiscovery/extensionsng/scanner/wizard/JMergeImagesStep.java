package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.BorderLayout;
import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;
import org.barrelorgandiscovery.bookimage.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.Messages;
import org.barrelorgandiscovery.extensionsng.scanner.OpenCVVideoFamilyImageSeeker;
import org.barrelorgandiscovery.extensionsng.scanner.merge.JScannerMergePanel;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.repository.Repository2;

public class JMergeImagesStep extends BasePanelStep {

	private static Logger logger = Logger.getLogger(JMergeImagesStep.class);

	public static int rescaleFactor = 1;
	public static int everyFrames = 5;
	
	private JScannerMergePanel mergePanel;

	private IFamilyImageSeeker perfoScanFolder;

	private IPrefsStorage ps;
	private Repository2 repository;

	public JMergeImagesStep(Step parent, IPrefsStorage prefsStorage, Repository2 repository2) throws Exception {
		super("mergeimagestep", parent); //$NON-NLS-1$
		this.ps = prefsStorage;
		this.repository = repository2;
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

		// there might have video files !

		File scanfolder = passedFolder;
		IFamilyImageSeeker imageScan = null;

		if (scanfolder.isDirectory()) {
			imageScan = new PerfoScanFolder(scanfolder);
		} else {
			imageScan = new OpenCVVideoFamilyImageSeeker(scanfolder, rescaleFactor, everyFrames);

		}
		assert imageScan != null;
		mergePanel = new JScannerMergePanel(imageScan, ps, repository);

		if (imageScan instanceof PerfoScanFolder) {
			int firstIndex = ((PerfoScanFolder) imageScan).getFirstImageIndex();
			if (firstIndex != -1) {
				logger.warn("no images in the folder"); //$NON-NLS-1$
				mergePanel.setCurrentImage(firstIndex);
			}
		}

		add(mergePanel, BorderLayout.CENTER);
	}

	@Override
	public String getLabel() {
		return Messages.getString("JMergeImagesStep.2"); //$NON-NLS-1$
	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {
		return null;
	}

	@Override
	public boolean isStepCompleted() {
		return false;
	}
}
