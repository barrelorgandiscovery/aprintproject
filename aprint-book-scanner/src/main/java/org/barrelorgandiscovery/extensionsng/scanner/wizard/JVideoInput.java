package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.BorderLayout;
import java.io.File;
import java.io.Serializable;

import javax.swing.JFrame;

import org.apache.commons.vfs2.FileObject;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensionsng.scanner.Messages;
import org.barrelorgandiscovery.gui.tools.BookmarkPanel;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.ui.tools.VFSTools;

import com.googlecode.vfsjfilechooser2.VFSJFileChooser;
import com.googlecode.vfsjfilechooser2.constants.VFSJFileChooserConstants;

/**
 * open a video
 * 
 * @author pfreydiere
 *
 */
public class JVideoInput extends BasePanelStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3504441252542222560L;

	private static final Logger logger = Logger.getLogger(JVideoInput.class);

	public JVideoInput(Step parent) {
		super("VideoInput", parent); //$NON-NLS-1$
		initComponents();
	}

	@Override
	public String getLabel() {
		return Messages.getString("JVideoInput.1"); //$NON-NLS-1$
	}

	private VFSJFileChooser fileChooser;
	
	private File selectedVideoFile = null;

	protected void initComponents() {

		setLayout(new BorderLayout());

		this.fileChooser = new VFSJFileChooser();
		// add panel
		BookmarkPanel p = new BookmarkPanel(fileChooser);
		fileChooser.setAccessory(p);
		fileChooser.setControlButtonsAreShown(false);
		add(fileChooser, BorderLayout.CENTER);

		fileChooser.addPropertyChangeListener(VFSJFileChooserConstants.SELECTED_FILES_CHANGED_PROPERTY, (s) -> {
			try {
				FileObject f = (FileObject) s.getNewValue();
				File file = VFSTools.convertToFile(f);

				this.selectedVideoFile = file;
				
				updateState();
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		});
		
		fileChooser.addActionListener( (a) -> {
			try {
				
				FileObject f = fileChooser.getSelectedFileObject();
				File file = VFSTools.convertToFile(f);

				this.selectedVideoFile = file;
				
				updateState();
				
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		});

	}

	private void updateState() {
		if (selectedVideoFile != null && stepListener != null) {
			stepListener.stepStatusChanged();
		}
	}
	
	private StepStatusChangedListener stepListener;

	@Override
	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {
		this.stepListener = stepListener;

	}

	@Override
	public Serializable unActivateAndGetSavedState() throws Exception {

		return selectedVideoFile;
	}

	@Override
	public boolean isStepCompleted() {
		return selectedVideoFile != null;
	}

	// test method
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		File f = new File("C:\\projets\\APrint\\contributions\\patrice\\2018_josephine_90degres\\perfo"); //$NON-NLS-1$

		JFrame frame = new JFrame();
		frame.setSize(800, 600);

		frame.getContentPane().setLayout(new BorderLayout());
		JVideoInput imagepreviewer = new JVideoInput(null);

		frame.getContentPane().add(imagepreviewer, BorderLayout.CENTER);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

}
