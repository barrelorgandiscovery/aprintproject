package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.scanner.FamilyImageFolder;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

import com.jeta.forms.components.panel.FormPanel;
import com.l2fprod.common.swing.JDirectoryChooser;

public class JChooseFolderStep extends BasePanelStep {

	/** */
	private static final long serialVersionUID = 5969685883805568698L;

	private static final String DEFAULTCURRENTFOLDER = "defaultcurrentfolder";

	private static Logger logger = Logger.getLogger(JChooseFolderStep.class);

	private IPrefsStorage ps;

	public JChooseFolderStep(IPrefsStorage ps) throws Exception {
		super("StepChooseFolder", null);
		this.ps = ps;
		assert ps != null;
		initComponents();
	}

	private JLabel folderLabel;

	private JLabel lblfilter;

	private JTextField txtfilter;
	
	private JLabel lblImagesPreview;

	private JImageFolderPreviewer imagefolderpreviewere = new JImageFolderPreviewer();

	protected void initComponents() throws Exception {

		imagefolderpreviewere.setPreferredSize(new Dimension(300,300));
		
		InputStream is = getClass().getResourceAsStream("perfoscanchoose.jfrm");
		assert is != null;
		FormPanel fp = new FormPanel(is);

		AbstractButton b = fp.getButton("btnchoose");
		b.setText("Choose Folder ...");

		b.addActionListener((e) -> {
			JDirectoryChooser c;
			try {
				Cursor old = JChooseFolderStep.this.getCursor();
				try {
					JChooseFolderStep.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					c = new JDirectoryChooser();
					if (folderChoosen != null) {
						c.setCurrentDirectory(folderChoosen);
					}

				} finally {
					JChooseFolderStep.this.setCursor(old);
				}
				int result = c.showOpenDialog(JChooseFolderStep.this);
				File selectedFolder = c.getSelectedFile();
				if (selectedFolder != null && selectedFolder.isDirectory() && result == JFileChooser.APPROVE_OPTION) {
					logger.debug("open the folder " + selectedFolder);
					folderChoosen = selectedFolder;
					updateState();

					ps.setFileProperty(DEFAULTCURRENTFOLDER, folderChoosen);
					ps.save();
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});

		folderLabel = fp.getLabel("lblfolder");
		
		lblfilter = fp.getLabel("lblfilter");
		lblfilter.setText("Specify regexp to filter images :");
		txtfilter = fp.getTextField("txtfilter");
		txtfilter.addKeyListener( new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
			@Override
			public void keyReleased(KeyEvent e) {
				JChooseFolderStep.this.updateState();
			}
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
		});
		
		lblImagesPreview = fp.getLabel("lblImagesPreview");
		lblImagesPreview.setText("Folder Image Preview :");

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

		fp.getFormAccessor().replaceBean("preview", imagefolderpreviewere);

		folderChoosen = ps.getFileProperty(DEFAULTCURRENTFOLDER, null);
		
		revalidate();
		updateState();
	}

	/** update state */
	private void updateState() {
		if (folderChoosen == null) {
			folderLabel.setText("< Choose Folder >");
		} else {
			folderLabel.setText(folderChoosen.getAbsolutePath());
			String inputFilePattern = txtfilter.getText();
			Pattern filePatternMatch = null;
			if (inputFilePattern != null && !inputFilePattern.trim().isEmpty()) {
			   filePatternMatch = Pattern.compile(inputFilePattern);
			}
			logger.debug("file pattern match :" + filePatternMatch);
			perfoScanFolder = new FamilyImageFolder(folderChoosen, filePatternMatch);
			if (folderChoosen != null) {
				try {
					if (listener != null)
						listener.stepStatusChanged();
					imagefolderpreviewere.setFilePattern(filePatternMatch);
					imagefolderpreviewere.loadFolder(folderChoosen);
				} catch (Exception ex) {
					logger.error("preview not available :" + ex.getMessage(), ex);
				}
			}
		}
		
		revalidate();
	}

	@Override
	public String getLabel() {
		return "Choose Images folder";
	}

	private File folderChoosen = null;

	private FamilyImageFolder perfoScanFolder = null;

	private StepStatusChangedListener listener = null;

	public static class ChooseFolderState implements Serializable {
		File folderChoosen;
		String pattern;
	}

	@Override
	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {

		if (state != null && state instanceof ChooseFolderState) {
			ChooseFolderState s = (ChooseFolderState) state;

			this.folderChoosen = s.folderChoosen;
			this.txtfilter.setText(s.pattern);
		}

		updateState();
		this.listener = stepListener;
	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {
		return folderChoosen;
	}

	@Override
	public boolean isStepCompleted() {
		return folderChoosen != null;
	}

	File getFolder() {
		return folderChoosen;
	}
}
