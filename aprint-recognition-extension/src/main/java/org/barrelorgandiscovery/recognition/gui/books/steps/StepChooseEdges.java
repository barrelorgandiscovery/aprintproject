package org.barrelorgandiscovery.recognition.gui.books.steps;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.BookImage;
import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.images.books.tools.BookImageRecognitionTiledImage;
import org.barrelorgandiscovery.images.books.tools.IFileFamilyTiledImage;
import org.barrelorgandiscovery.images.books.tools.RecognitionTiledImage;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.recognition.gui.books.BookReadProcessor;
import org.barrelorgandiscovery.recognition.gui.books.JScaleDisplayLayer;
import org.barrelorgandiscovery.recognition.gui.books.states.EdgesStates;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.IInstrumentName;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.INumericImage;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JLinesLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JTiledImageDisplayLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.JViewingToolBar;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.MoveAndCreatePointForMultipleLinesTool;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.MoveTool;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * this step permit the user to define/adjust the book borders to be able to
 * take the interesting part of the book to recognize
 * 
 * @author pfreydiere
 * 
 */
public class StepChooseEdges extends BasePanelStep implements Step {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8702037126200332798L;

	private static Logger logger = Logger.getLogger(StepChooseEdges.class);

	/**
	 * display the book
	 */
	private JDisplay display;
	private JTiledImageDisplayLayer imageDisplayLayer;
	private JViewingToolBar viewingToolBar;

	private EdgesStates currentState;

	private StepStatusChangedListener stepStatusChangedListener;

	private JLinesLayer top;
	private JLinesLayer bottom;

	private JScaleDisplayLayer scaleDisplayLayer;

	private Repository2 repository;
	
	private JCheckBox holeLine;

	private JCheckBox reverseScaleCombo;

	public StepChooseEdges(String id, Step parent, Repository2 repository) throws Exception {
		super(id, parent);
		this.repository = repository;
		initComponent();
	}

	private void initComponent() throws Exception {

		this.display = new JDisplay();
		setLayout(new BorderLayout());
		add(display, BorderLayout.CENTER);

		imageDisplayLayer = new JTiledImageDisplayLayer(display);

		display.addLayer(imageDisplayLayer);

		// add the toolbars
		viewingToolBar = new JViewingToolBar(display);
		add(viewingToolBar, BorderLayout.NORTH);

		top = new JLinesLayer();
		display.addLayer(top);
		bottom = new JLinesLayer();
		display.addLayer(bottom);

		JButton buttonChooseMoveEdges = new JButton();
		buttonChooseMoveEdges.setToolTipText(Messages.getString("StepChooseEdges.0")); //$NON-NLS-1$
		buttonChooseMoveEdges.setIcon(ImageTools.loadIcon(getClass(), "tablet.png")); //$NON-NLS-1$
		buttonChooseMoveEdges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					top.setSelected(Collections.EMPTY_LIST);
					display.setCurrentTool(
							new MoveAndCreatePointForMultipleLinesTool(display, new JLinesLayer[] { top, bottom }));
				} catch (Exception ex) {
					logger.error(Messages.getString("StepChooseEdges.1") + ex.getMessage(), ex); //$NON-NLS-1$
				}
			}
		});
		viewingToolBar.addSeparator();
		viewingToolBar.add(buttonChooseMoveEdges);
		
		
		JButton btnMoveEdges = new JButton();
		btnMoveEdges.setText("Move edges");
		btnMoveEdges.addActionListener( (e) -> {
			try {
				display.setCurrentTool(new MoveTool(display, new JLinesLayer[] { top, bottom }));
			} catch(Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});
		viewingToolBar.add(btnMoveEdges);
		
		viewingToolBar.addSeparator();
		reverseScaleCombo = new JCheckBox(Messages.getString("StepChooseEdges.2")); //$NON-NLS-1$
		viewingToolBar.add(reverseScaleCombo);

		
		scaleDisplayLayer = new JScaleDisplayLayer(display, null, top, bottom);
		display.addLayer(scaleDisplayLayer);

		
		reverseScaleCombo.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JCheckBox cbs = (JCheckBox) e.getSource();
				scaleDisplayLayer.setViewInverted(cbs.isSelected());
				display.repaint();
			}
		});

	}

	@Override
	public String getLabel() {
		return Messages.getString("StepChooseEdges.3"); //$NON-NLS-1$
	}

	@Override
	public void activate(Serializable state, WizardStates states, StepStatusChangedListener stepListener)
			throws Exception {

		try {
			currentState = (EdgesStates) state;
		} catch (Exception ex) {
			logger.error("error getting the state, creating a new one :" //$NON-NLS-1$
					+ ex.getMessage(), ex);
		}

		this.stepStatusChangedListener = stepListener;

		// get instrument

		IInstrumentName in = states.getPreviousStateImplementing(this, IInstrumentName.class);

		Instrument ins = repository.getInstrument(in.getInstrumentName());
		if (ins == null)
			throw new Exception(Messages.getString("StepChooseEdges.4") + in.getInstrumentName() + Messages.getString("StepChooseEdges.5")); //$NON-NLS-1$ //$NON-NLS-2$
		scaleDisplayLayer.setInstrumentScale(ins.getScale());

		// previous step
		INumericImage previousStateImplementing = states.getPreviousStateImplementing(this, INumericImage.class);

		assert previousStateImplementing != null;

		File imageFile = previousStateImplementing.getImageFile();
		
		// read the imageSize
		IFileFamilyTiledImage tileImage = null;
		if (imageFile.getName().endsWith(BookImage.BOOKIMAGE_EXTENSION)) {
		
			tileImage = new BookImageRecognitionTiledImage(new ZipBookImage(imageFile));
			
		} else {

			RecognitionTiledImage recTiledImage = new RecognitionTiledImage(imageFile);
			recTiledImage.constructTiles();
			tileImage = recTiledImage;
			

		}
		
		
		imageDisplayLayer.setImageToDisplay(tileImage);

		if (currentState == null) {
			currentState = new EdgesStates();
		}

		if (currentState.top == null || currentState.bottom == null || currentState.top.size() < 2
				|| currentState.bottom.size() < 2) {

			currentState.top = new ArrayList<Point2D.Double>();
			currentState.bottom = new ArrayList<Point2D.Double>();

			double factor = 0.1;

			currentState.top.add(new Point2D.Double(0, factor * tileImage.getHeight()));
			currentState.top.add(new Point2D.Double(tileImage.getWidth(), factor * tileImage.getHeight()));

			factor = 0.9;

			currentState.bottom.add(new Point2D.Double(0, factor * tileImage.getHeight()));
			currentState.bottom.add(new Point2D.Double(tileImage.getWidth(), factor * tileImage.getHeight()));

		}

		top.clear();
		top.addAll(BookReadProcessor.fromPoint(currentState.top));

		bottom.clear();
		bottom.addAll(BookReadProcessor.fromPoint(currentState.bottom));

		reverseScaleCombo.setSelected(currentState.viewInverted);
		scaleDisplayLayer.setViewInverted(currentState.viewInverted);
		
	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {

		currentState.top = BookReadProcessor.toPoint(new ArrayList<>(top.getGraphics()));
		currentState.bottom = BookReadProcessor.toPoint(new ArrayList<>(bottom.getGraphics()));
		currentState.viewInverted = reverseScaleCombo.isSelected();

		return currentState;
	}

	@Override
	public boolean isStepCompleted() {
		return true;
	}

	@Override
	public Icon getPageImage() {
		return null;
	}

	
	
}
