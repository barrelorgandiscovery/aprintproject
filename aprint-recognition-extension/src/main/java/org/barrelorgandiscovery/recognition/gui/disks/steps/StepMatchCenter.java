package org.barrelorgandiscovery.recognition.gui.disks.steps;

import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.ImageFileAndInstrument;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.INumericImage;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.PointsAndEllipseParameters;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JEllipticLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JImageDisplayLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.LayerChangedListener;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.CreatePointTool;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.JViewingToolBar;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.StringTools;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * Step for getting the center of the disk
 * 
 * @author pfreydiere
 * 
 */
public class StepMatchCenter extends BasePanelStep implements Step {

	private static Logger logger = Logger.getLogger(StepMatchCenter.class);

	private JDisplay display;

	private JImageDisplayLayer imageDisplayLayer;

	private JEllipticLayer ellipticLayer;

	private StepStatusChangedListener stepStatusChangedListener;

	private PointsAndEllipseParameters currentState;

	private String label;

	private Repository2 repository;

	public StepMatchCenter(String id, Step parent, String label,
			JEllipticLayer ellipticLayer, Repository2 repository)
			throws Exception {
		super(id, parent);
		this.label = label;
		this.ellipticLayer = ellipticLayer;
		this.repository = repository;
		initComponent();

	}

	private void initComponent() throws Exception {

		display = new JDisplay();
		imageDisplayLayer = new JImageDisplayLayer();

		display.addLayer(imageDisplayLayer);

		display.addLayer(ellipticLayer);

		ellipticLayer.addLayerChangedListener(new LayerChangedListener() {

			public void layerSelectionChanged() {

			}

			public void layerContentChanged() {
				if (stepStatusChangedListener != null) {
					stepStatusChangedListener.stepStatusChanged();
				}
			}
		});

		setLayout(new BorderLayout());

		FormPanel fp = new FormPanel(getClass().getResourceAsStream(
				"diskandparameters.jfrm")); //$NON-NLS-1$

		add(fp, BorderLayout.CENTER);

		FormAccessor formAccessor = fp.getFormAccessor();
		formAccessor.replaceBean("canvas", display); //$NON-NLS-1$

		CreatePointTool createPointTool = new CreatePointTool(display,
				ellipticLayer);

		JViewingToolBar toolbar = new JViewingToolBar(display);

		toolbar.addSeparator();

		JToggleButton button = toolbar.addTool(createPointTool);
		button.setIcon(new ImageIcon(CreatePointTool.class
				.getResource("kedit.png"))); //$NON-NLS-1$
		button.setText(Messages.getString("StepMatchCenter.3")); //$NON-NLS-1$
		button.setToolTipText(Messages.getString("StepMatchCenter.4")); //$NON-NLS-1$

		display.setCurrentTool(createPointTool);

		formAccessor.replaceBean("toolbar", toolbar); //$NON-NLS-1$

	}

	public String getLabel() {
		return label;
	}

	public void activate(Serializable state, WizardStates states,
			StepStatusChangedListener stepListener) throws Exception {

		try {
			currentState = (PointsAndEllipseParameters) state;
		} catch (Exception ex) {
			logger.error("error getting the state, creating a new one :" //$NON-NLS-1$
					+ ex.getMessage(), ex);
		}

		if (currentState == null) {
			currentState = new PointsAndEllipseParameters();
		}

		this.stepStatusChangedListener = stepListener;

		// previous step
		INumericImage previousStateImplementing = states
				.getPreviousStateImplementing(this, INumericImage.class);

		assert previousStateImplementing != null;

		BufferedImage bi = ImageTools.loadImage(previousStateImplementing
				.getImageFile().toURL());
		imageDisplayLayer.setImageToDisplay(bi);
		if (currentState.points != null) {
			ellipticLayer.clear();
			for (Rectangle2D.Double d : currentState.points) {
				ellipticLayer.add(d);
			}
		}

		if (currentState.ellipseParameters != null) {
			ellipticLayer.setEllipseParameters(currentState.ellipseParameters);
		}

		if (ellipticLayer instanceof JDiskTracksCorrectedLayer) {
			// previous step
			ImageFileAndInstrument previousInstrumentChoice = states
					.getPreviousStateImplementing(this,
							ImageFileAndInstrument.class);
			assert previousInstrumentChoice != null;

			String instrumentName = previousInstrumentChoice.instrumentName;
			assert StringTools.nullIfEmpty(instrumentName) != null;

			Instrument ins = repository.getInstrument(instrumentName);
			assert ins != null;

			Scale scale = ins.getScale();

			((JDiskTracksCorrectedLayer) ellipticLayer).setScale(scale);

			PointsAndEllipseParameters pdiskparameters = states
					.getPreviousStateImplementing(this,
							PointsAndEllipseParameters.class);
			((JDiskTracksCorrectedLayer) ellipticLayer)
					.setRealCenter(pdiskparameters.ellipseParameters.centre);

		}

		display.fit();

	}

	public Serializable unActivateAndGetSavedState() throws Exception {
		this.stepStatusChangedListener = null;

		currentState.ellipseParameters = ellipticLayer
				.getCurrentEllipseParameters();
		currentState.points = new ArrayList<Rectangle2D.Double>(
				ellipticLayer.getGraphics());

		return currentState;
	}

	public boolean isStepCompleted() {
		if (ellipticLayer.getCurrentEllipseParameters() == null)
			return false;

		if (ellipticLayer.getGraphics() == null
				|| ellipticLayer.getGraphics().size() < 5)
			return false;

		return true;
	}

	public String getId() {
		return id;
	}

}
