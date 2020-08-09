package org.barrelorgandiscovery.recognition.gui.disks.steps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.IInstrumentChoiceListener;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.JCoverFlowInstrumentChoiceWithFilter;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.ImageFileAndInstrument;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JImageDisplayLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.JViewingToolBar;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.ui.tools.VFSTools;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

public class StepChooseFilesAndInstrument extends BasePanelStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5849877785127366190L;
	
	
	private static final String CURRENT_DIRECTORY_PREF = "currentDirectory"; //$NON-NLS-1$
	private static final String LASTBOOKIMAGE_FILE_PREF = "lastBookImageOpened"; //$NON-NLS-1$

	private static Logger logger = Logger.getLogger(StepChooseFilesAndInstrument.class);

	/**
	 * button for choosing the picture file
	 */
	private JButton browsePicture;

	/**
	 * the image display component
	 */
	private JDisplay display;
	/**
	 * ImageDisplayer
	 */
	private JImageDisplayLayer layer;
	/**
	 * initial image file
	 */
	private File imageFile;
	/**
	 * name of instrument
	 */
	private String instrumentName;

	/**
	 * instrument repository
	 */
	private Repository2 repository;

	/**
	 * component for choosing the instrument
	 */
	private JCoverFlowInstrumentChoiceWithFilter instrumentChooser;

	/**
	 * step listener
	 */
	private StepStatusChangedListener stepChangedListener;

	private ImageFileAndInstrument currentState;

	private IPrefsStorage prefStorage;

	/**
	 * constructor
	 * 
	 * @param repository the instrument repository
	 * @throws Exception
	 */
	public StepChooseFilesAndInstrument(String id, Step parent, Repository2 repository, IPrefsStorage prefStorage)
			throws Exception {
		super(id, parent);
		assert repository != null;
		this.repository = repository;
		assert prefStorage != null;
		this.prefStorage = prefStorage;
		initComponents();

	}

	/**
	 * init swing components
	 * 
	 * @throws Exception
	 */
	private void initComponents() throws Exception {

		FormPanel fp = new FormPanel(getClass().getResourceAsStream("pictureandinstrument.jfrm")); //$NON-NLS-1$
		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

		browsePicture = (JButton) fp.getComponentByName("picturebrowse"); //$NON-NLS-1$
		browsePicture.setIcon(new ImageIcon(getClass().getResource("folder.png"))); //$NON-NLS-1$
		browsePicture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					File lastLocation = prefStorage.getFileProperty(CURRENT_DIRECTORY_PREF, null);
					File lastFile = prefStorage.getFileProperty(LASTBOOKIMAGE_FILE_PREF, null);
					APrintFileChooser fc = new APrintFileChooser(lastFile);
					if (lastLocation != null && lastFile == null) {
						fc.setCurrentDirectory(lastLocation);
					}
					int ret = fc.showOpenDialog(StepChooseFilesAndInstrument.this);
					if (ret == APrintFileChooser.APPROVE_OPTION) {
						AbstractFileObject selectedFile = fc.getSelectedFile();
						File f = VFSTools.convertToFile(selectedFile);
						internalChangeImageFile(f);
					}
				} catch (Exception ex) {
					logger.error("error in reading the file :" + ex.getMessage(), ex); //$NON-NLS-1$
					JMessageBox.showError(this, ex);
				}
			}
		});

		display = new JDisplay();

		this.layer = new JImageDisplayLayer();
		display.addLayer(layer);

		FormAccessor formAccessor = fp.getFormAccessor();
		formAccessor.replaceBean("picturepreview", display); //$NON-NLS-1$

		Component tbplacement = fp.getComponentByName("toolbar"); //$NON-NLS-1$
		formAccessor.replaceBean(tbplacement, new JViewingToolBar(display));

		instrumentChooser = new JCoverFlowInstrumentChoiceWithFilter(repository, new IInstrumentChoiceListener() {
			public void instrumentChanged(Instrument newInstrument) {
				if (newInstrument != null) {
					instrumentName = newInstrument.getName();
				} else {
					instrumentName = null;
				}
				checkStateAndCallStepChangedListener();
			}
		});
		instrumentChooser.setPreferredSize(new Dimension(200, 200));

		formAccessor.replaceBean("instrumentselect", instrumentChooser); //$NON-NLS-1$

		// labels
		JLabel labelChooseInstrument = formAccessor.getLabel("picturephaseselection"); //$NON-NLS-1$
		labelChooseInstrument.setText(Messages.getString("StepChooseFilesAndInstrument.1")); //$NON-NLS-1$

		JLabel selectInstrumentLabel = formAccessor.getLabel("selectInstrument"); //$NON-NLS-1$

		selectInstrumentLabel.setText(Messages.getString("StepChooseFilesAndInstrument.3")); //$NON-NLS-1$
		this.imageFile = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.recognition.gui.wizard.Step#activate(java.io
	 * .Serializable)
	 */
	public void activate(Serializable state, WizardStates states, StepStatusChangedListener stepChangedListener)
			throws Exception {

		if (state == null || (!(state instanceof ImageFileAndInstrument))) {
			currentState = new ImageFileAndInstrument();
		} else {
			currentState = (ImageFileAndInstrument) state;
		}
		assert currentState != null;

		internalChangeImageFile(currentState.diskFile);

		display.fit();

		internalChangeInstrumentName(currentState.instrumentName);

		this.stepChangedListener = stepChangedListener;

	}

	public void setImageFile(File newImageFile) throws Exception {
		currentState.diskFile = newImageFile;
		internalChangeImageFile(newImageFile);
	}

	public File getImageFile() {
		return currentState.diskFile;
	}

	public void fitImage() {
		display.fit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.recognition.gui.wizard.Step#unActivate(java.
	 * io.Serializable)
	 */
	public Serializable unActivateAndGetSavedState() throws Exception {

		currentState.diskFile = imageFile;
		currentState.instrumentName = instrumentName;
		this.stepChangedListener = null;
		return currentState;
	}

	public boolean isStepCompleted() {
		return instrumentName != null && imageFile != null;
	}

	public String getLabel() {
		return Messages.getString("StepChooseFilesAndInstrument.8"); //$NON-NLS-1$
	}

	/**
	 * @param selectedFile
	 * @throws Exception
	 * @throws MalformedURLException
	 */
	private void internalChangeImageFile(File selectedFile) throws Exception, MalformedURLException {
		// load the file
		BufferedImage bi = null;
		if (selectedFile != null) {
			try {
				bi = ImageTools.loadImage(selectedFile);
			} catch (Exception ex) {
				logger.error("error while loading the image :" + ex.getMessage(), ex); //$NON-NLS-1$
			}
		}
		layer.setImageToDisplay(bi);
		imageFile = selectedFile;
		display.fit();
		checkStateAndCallStepChangedListener();
	}

	private void internalChangeInstrumentName(String instrumentName) throws Exception {
		instrumentChooser.selectInstrument(instrumentName);
		this.instrumentName = instrumentName;
		checkStateAndCallStepChangedListener();
	}

	private void checkStateAndCallStepChangedListener() {
		if (instrumentName != null && imageFile != null) {
			if (stepChangedListener == null)
				logger.warn("stepChangedListener must be non null"); //$NON-NLS-1$
			else
				stepChangedListener.stepStatusChanged();
		}
	}

}
