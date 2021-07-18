package org.barrelorgandiscovery.extensionsng.scanner.wizardscan;

import java.awt.BorderLayout;
import java.io.InputStream;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensionsng.scanner.Messages;
import org.barrelorgandiscovery.extensionsng.scanner.scan.IChooseWebCamListener;
import org.barrelorgandiscovery.extensionsng.scanner.scan.JChooseWebCam;
import org.barrelorgandiscovery.extensionsng.scanner.scan.JChooseWebCam.WebCamConfig;
import org.barrelorgandiscovery.extensionsng.scanner.scan.JTriggerComponent;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.ITriggerFactory;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.ITriggerFeedback;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.tools.Disposable;

import com.github.sarxos.webcam.Webcam;
import com.jeta.forms.components.panel.FormPanel;

public class JScanParameterStep extends BasePanelStep implements Disposable {

	/** */
	private static final long serialVersionUID = -7296047711004950598L;

	private static Logger logger = Logger.getLogger(JScanParameterStep.class);

	private IPrefsStorage preferences;

	private JChooseWebCam webcamChooser;

	private JTriggerComponent jTriggerComponent;

	private IExtension[] extensions;

	public JScanParameterStep(Step parent, IPrefsStorage preferences, IExtension[] extensions) throws Exception {
		super("scanparameter", parent); //$NON-NLS-1$
		this.preferences = preferences;
		this.extensions = extensions;
		initComponents();
	}

	protected void initComponents() throws Exception {
		InputStream isform = getClass().getResourceAsStream("parameterpanel.jfrm"); //$NON-NLS-1$
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

		jTriggerComponent = new JTriggerComponent(preferences, extensions);

		fp.getFormAccessor().replaceBean("lblwebcam", webcamChooser); //$NON-NLS-1$
		fp.getFormAccessor().replaceBean("lbltrigger", jTriggerComponent); //$NON-NLS-1$
		
		JLabel lclchooseTrigger = fp.getLabel("lblchooseimagetrigger");//$NON-NLS-1$
		lclchooseTrigger.setText("Choose the trigger for taking snapshots");

		setLayout(new BorderLayout());
		
		JScrollPane scrollPane = new JScrollPane(fp);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		add(scrollPane, BorderLayout.CENTER);
	}

	@Override
	public String getLabel() {
		return Messages.getString("JScanParameterStep.4"); //$NON-NLS-1$
	}

	public ITriggerFactory getTriggerFactory(ITriggerFeedback triggerFeedback) throws Exception {
		return jTriggerComponent.createTriggerFactory(triggerFeedback);
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
		logger.debug("stop the preview"); //$NON-NLS-1$
		webcamChooser.stopPreview();
		return null;
	}

	@Override
	public void dispose() {
		try {
			webcamChooser.dispose();
		} catch (Throwable t) {
		}
		
		try {
			webcamChooser.dispose();
		} catch (Throwable t) {
		}
		
		
		
	}
}
