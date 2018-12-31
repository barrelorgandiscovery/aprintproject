package org.barrelorgandiscovery.extensionsng.scanner.wizardscan;

import java.awt.BorderLayout;
import java.io.InputStream;
import java.io.Serializable;

import org.barrelorgandiscovery.extensionsng.scanner.scan.IChooseWebCamListener;
import org.barrelorgandiscovery.extensionsng.scanner.scan.JChooseWebCam;
import org.barrelorgandiscovery.extensionsng.scanner.scan.JChooseWebCam.WebCamConfig;
import org.barrelorgandiscovery.extensionsng.scanner.scan.JTriggerComponent;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.ITriggerFactory;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

import com.github.sarxos.webcam.Webcam;
import com.jeta.forms.components.panel.FormPanel;

public class JScanParameterStep extends BasePanelStep {

	/** */
	private static final long serialVersionUID = -7296047711004950598L;

	private IPrefsStorage preferences;

	private JChooseWebCam webcamChooser;

	private JTriggerComponent jTriggerComponent;

	public JScanParameterStep(Step parent, IPrefsStorage preferences) throws Exception {
		super("scanparameter", parent);
		this.preferences = preferences;
		initComponents();
	}

	protected void initComponents() throws Exception {
		InputStream isform = getClass().getResourceAsStream("parameterpanel.jfrm");
		assert isform != null;
		FormPanel fp = new FormPanel(isform);

		webcamChooser = new JChooseWebCam();
		webcamChooser.setChooseWebCamListener(new IChooseWebCamListener() {
			@Override
			public void choosedWebCamChanged(WebCamConfig webcamConfig) {
				if (stepListener != null) {
					stepListener.stepStatusChanged();
				}
			}
		});

		jTriggerComponent = new JTriggerComponent(preferences);

		fp.getFormAccessor().replaceBean("lblwebcam", webcamChooser);
		fp.getFormAccessor().replaceBean("lbltrigger", jTriggerComponent);

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);
	}

	@Override
	public String getLabel() {
		return "Choose scan parameters";
	}

	public ITriggerFactory getTriggerFactory() throws Exception {
		return jTriggerComponent.createTriggerFactory();
	}

	public Webcam getOpenedWebCam() {
		WebCamConfig config = webcamChooser.getSelectedWebCamConfig();
		if (config == null) {
			return null;
		}

		Webcam currentWebCam = config.webcam;
		assert currentWebCam.isOpen();
		return currentWebCam;
	}

	@Override
	public boolean isStepCompleted() {
		WebCamConfig currentConfig = webcamChooser.getSelectedWebCamConfig();
		return currentConfig != null && currentConfig.webcam != null;
	}

	StepStatusChangedListener stepListener;

	@Override
	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {
		this.stepListener = stepListener;
		webcamChooser.startPreview();
	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {
		webcamChooser.stopPreview();
		return null;
	}
}
