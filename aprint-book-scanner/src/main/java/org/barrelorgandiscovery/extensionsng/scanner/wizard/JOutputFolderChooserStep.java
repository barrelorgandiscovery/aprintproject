package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.PerfoScanFolder;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.tools.ImageTools;

import com.jeta.forms.components.panel.FormPanel;
import com.l2fprod.common.swing.JDirectoryChooser;

/**
 * Step for choosing the output folder for generating image files
 * 
 * @author pfreydiere
 *
 */
public class JOutputFolderChooserStep extends BasePanelStep {

	private static Logger logger = Logger.getLogger(JOutputFolderChooserStep.class);

	private File folderChoosen;

	private StepStatusChangedListener listener;

	private JLabel lbloutputfolder;

	private JLabel folderLabel;

	private AbstractButton btnfolder;
	
	private IPrefsStorage ps;
	
	private static final String DEFAULTCURRENTFOLDER = "defaultfolderstorage";

	public JOutputFolderChooserStep(Step parent, IPrefsStorage ps) throws Exception {
		super("outputfolder", parent);
		this.ps = ps;
		initComponents();
	}

	/** update state */
	private void updateState() {
		if (folderChoosen == null) {
			folderLabel.setText("< Choose Folder >");
		} else {
			folderLabel.setText(folderChoosen.getAbsolutePath());

		}
		if (folderChoosen != null) {
			try {
				if (listener != null)
					listener.stepStatusChanged();
			} catch (Exception ex) {
				logger.error("preview not available :" + ex.getMessage(), ex);
			}
		}
	}

	protected void initComponents() throws Exception {
		InputStream formStream = getClass().getResourceAsStream("outputfolder.jfrm");
		FormPanel fp = new FormPanel(formStream);

		lbloutputfolder = fp.getLabel("lbloutputfolder");
		lbloutputfolder.setText("Choose in which folder you will save the pictures ...");
		folderLabel = fp.getLabel("lblfolder");
		//

		btnfolder = fp.getButton("btnfolder");
		btnfolder.setText("Choose Folder ...");
		btnfolder.setIcon(ImageTools.loadIcon(getClass(), "folder.png"));
		btnfolder.addActionListener((e) -> {

			JDirectoryChooser c;
			try {
				Cursor old = JOutputFolderChooserStep.this.getCursor();
				try {
					JOutputFolderChooserStep.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					c = new JDirectoryChooser();
					if (folderChoosen != null) {
						c.setCurrentDirectory(folderChoosen);
					}

				} finally {
					JOutputFolderChooserStep.this.setCursor(old);
				}
				int result = c.showOpenDialog(JOutputFolderChooserStep.this);
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

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

	}

	public void setLabelOutputFolder(String labelFolder) {
		this.lbloutputfolder.setText(labelFolder);
	}

	@Override
	public String getLabel() {
		return "Choose output folder";
	}

	@Override
	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {

		if (state != null && state instanceof File) {
			this.folderChoosen = (File) state;
		}

		updateState();
		this.listener = stepListener;
	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {
		return folderChoosen;
	}

	@Override
	public boolean isStepCompleted() { // TODO Auto-generated method stub
		return folderChoosen != null;
	}

	public File getFolder() {
		return folderChoosen;
	}
	

}
