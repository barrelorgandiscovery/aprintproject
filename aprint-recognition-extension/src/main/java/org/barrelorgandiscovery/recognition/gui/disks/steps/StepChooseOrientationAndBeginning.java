package org.barrelorgandiscovery.recognition.gui.disks.steps;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.INumericImage;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.PointsAndEllipsisParameters;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JImageDisplayLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JShapeLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.LayerChangedListener;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.CreatePointTool;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.JViewingToolBar;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.tools.ImageTools;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * Step for the beginning of the disk
 * 
 * @author pfreydiere
 * 
 */
public class StepChooseOrientationAndBeginning extends BasePanelStep implements
		Step {

	public static class AngleAndOrientation implements Serializable {
		public Rectangle2D.Double pointForAngle;
		public boolean trigo = true;
	}

	private static Logger logger = Logger
			.getLogger(StepChooseOrientationAndBeginning.class);

	private JDisplay display;

	private JImageDisplayLayer imageDisplayLayer;

	private JShapeLayer<Rectangle2D.Double> lineLayer;

	private StepStatusChangedListener stepStatusChangedListener;

	private AngleAndOrientation currentState;

	private String label;

	private Point2D.Double diskCenterPoint = null;

	public StepChooseOrientationAndBeginning(String id, Step parent,
			String label) throws Exception {
		super(id, parent);
		this.label = label;
		initComponent();

	}

	private void initComponent() throws Exception {

		display = new JDisplay();
		imageDisplayLayer = new JImageDisplayLayer();

		display.addLayer(imageDisplayLayer);

		lineLayer = new JShapeLayer<Rectangle2D.Double>() {

			@Override
			public void drawLayer(Graphics2D g2d) {
				super.drawLayer(g2d);

				List<Double> g = getGraphics();

				if (g != null) {

					for (Double d : g) {

						if (diskCenterPoint != null) {
							// draw lines
							Line2D.Double l2d = new Line2D.Double(
									diskCenterPoint, new Point2D.Double(
											d.getCenterX(), d.getCenterY())

							);
							g2d.draw(l2d);
						}

					}
				}

			}

		};

		display.addLayer(lineLayer);

		lineLayer.addLayerChangedListener(new LayerChangedListener() {

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
				lineLayer, 1); // 2 points max

		JViewingToolBar toolbar = new JViewingToolBar(display);

		toolbar.addSeparator();

		JToggleButton button = toolbar.addTool(createPointTool);
		button.setIcon(new ImageIcon(CreatePointTool.class
				.getResource("kedit.png"))); //$NON-NLS-1$
		button.setText(Messages.getString("StepChooseOrientationAndBeginning.3")); //$NON-NLS-1$
		button.setToolTipText(Messages.getString("StepChooseOrientationAndBeginning.4")); //$NON-NLS-1$

		display.setCurrentTool(createPointTool);

		formAccessor.replaceBean("toolbar", toolbar); //$NON-NLS-1$

	}

	public String getLabel() {
		return label;
	}

	public void activate(Serializable state, WizardStates states,
			StepStatusChangedListener stepListener) throws Exception {

		try {
			currentState = (AngleAndOrientation) state;
		} catch (Exception ex) {
			logger.error(
					"error getting the state, creating a new one :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
		}

		if (currentState == null) {
			currentState = new AngleAndOrientation();
		}

		this.stepStatusChangedListener = stepListener;

		// previous step for the image
		INumericImage previousStateImplementing = states
				.getPreviousStateImplementing(this, INumericImage.class);

		assert previousStateImplementing != null;

		BufferedImage bi = ImageTools.loadImage(previousStateImplementing
				.getImageFile());
		imageDisplayLayer.setImageToDisplay(bi);

		PointsAndEllipsisParameters centerEllipse = states
				.getPreviousStateImplementing(getParentStep(),
						PointsAndEllipsisParameters.class);

		if (centerEllipse != null && centerEllipse.outerEllipseParameters != null) {
			this.diskCenterPoint = (Point2D.Double) (centerEllipse.outerEllipseParameters.centre
					.clone());
		}

		if (currentState.pointForAngle != null) {
			lineLayer.clear();
			lineLayer.add(currentState.pointForAngle);
		}

		display.fit();

	}

	public Serializable unActivateAndGetSavedState() throws Exception {
		this.stepStatusChangedListener = null;

		Double o = lineLayer.getGraphics().get(0);
		currentState.pointForAngle = o;

		return currentState;
	}

	public boolean isStepCompleted() {

		if (lineLayer.getGraphics() == null
				|| lineLayer.getGraphics().size() < 1)
			return false;

		return true;
	}

}
