package org.barrelorgandiscovery.extensionsng.scanner.wizard.scanormerge;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.Wizard;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

import com.jeta.forms.components.panel.FormPanel;

public class JScanOrMergeStep extends BasePanelStep {

	/** */
	private static final long serialVersionUID = 237830858455778137L;

	IPrefsStorage preferences;
	private JRadioButton rbconstruct;
	private JRadioButton rbscan;
	private JRadioButton rbvideo;

	private ButtonGroup buttonGroup;

	private List<Step> mergeSteps;
	private List<Step> scanSteps;
	private List<Step> videoScanSteps;

	private Wizard wizard;

	public JScanOrMergeStep(Step parent, List<Step> mergeSteps, List<Step> scanSteps, List<Step> videoScanSteps,
			IPrefsStorage preferences) throws Exception {
		super("scanparameter", parent);
		this.preferences = preferences;
		this.scanSteps = scanSteps;
		this.mergeSteps = mergeSteps;
		this.videoScanSteps = videoScanSteps;
		initComponents();
	}

	public void initWizard(Wizard wizard) {
		this.wizard = wizard;
	}

	public boolean isScanSelected() {
		return rbscan.isSelected();
	}

	public boolean isVideoSelected() {
		return rbvideo.isSelected();
	}

	protected void initComponents() throws Exception {

		setLayout(new BorderLayout());

		FormPanel fp = new FormPanel(JScanOrMergeStep.class.getResourceAsStream("scanormergepanel.jfrm"));

		rbconstruct = fp.getRadioButton("rbconstruct");
		rbconstruct.setText("Merge images to create a full book image");

		JLabel lblmerge = fp.getLabel("lblmerge");
		lblmerge.setText("");
		lblmerge.setIcon(new ImageIcon(getClass().getResource("images.png")));

		JLabel lblscan = fp.getLabel("lblscan");
		lblscan.setIcon(new ImageIcon(getClass().getResource("webcam.jpg")));
		lblscan.setText("");

		JLabel lblvideo = fp.getLabel("lblvideo");
		lblvideo.setText("");

		rbscan = fp.getRadioButton("rbscan");
		rbscan.setText("use camera to scan a book");

		rbvideo = fp.getRadioButton("rbvideo");
		rbvideo.setText("use video file to construct book image");

		buttonGroup = new ButtonGroup();
		buttonGroup.add(rbconstruct);
		buttonGroup.add(rbscan);
		buttonGroup.add(rbvideo);

		ActionListener refreshWizardState = (e) -> {
			if (this.stepListener != null) {

				if (isScanSelected()) {
					wizard.changeFurtherStepList(scanSteps);
				} else if (isVideoSelected()) {
					wizard.changeFurtherStepList(videoScanSteps);
				} else {
					wizard.changeFurtherStepList(mergeSteps);
				}

				stepListener.stepStatusChanged();
			}
		};

		rbconstruct.addActionListener(refreshWizardState);
		rbscan.addActionListener(refreshWizardState);
		rbvideo.addActionListener(refreshWizardState);
		
		add(fp, BorderLayout.CENTER);
	}

	@Override
	public String getLabel() {
		return "Choose Scan activity";
	}

	StepStatusChangedListener stepListener;

	@Override
	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {
		this.stepListener = stepListener;
	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {
		return null;
	}

	@Override
	public boolean isStepCompleted() {
		return buttonGroup.getSelection() != null;
	}
}
