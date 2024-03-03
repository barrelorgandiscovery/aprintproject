package org.barrelorgandiscovery.gui.tools;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JDialog;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.prefs.PrefixedNamePrefsStorage;
import org.barrelorgandiscovery.tools.VFSTools;

import com.googlecode.vfsjfilechooser2.VFSJFileChooser;
import com.googlecode.vfsjfilechooser2.VFSJFileChooser.RETURN_TYPE;
import com.googlecode.vfsjfilechooser2.VFSJFileChooser.SELECTION_MODE;

/**
 * contain the JFileChooser, to permit bootstrap file selection and creation
 * 
 * @author pfreydiere
 *
 */
public class APrintFileChooser {

	private static Logger logger = Logger.getLogger(APrintFileChooser.class);

	/** Instruction to display only files. */
	public static final int FILES_ONLY = 0;

	/** Instruction to display only directories. */
	public static final int DIRECTORIES_ONLY = 1;

	/** Instruction to display both files and directories. */
	public static final int FILES_AND_DIRECTORIES = 2;

	// ************************
	// ***** Dialog Types *****
	// ************************

	/**
	 * Type value indicating that the <code>JFileChooser</code> supports an "Open"
	 * file operation.
	 */
	public static final int OPEN_DIALOG = 0;

	/**
	 * Type value indicating that the <code>JFileChooser</code> supports a "Save"
	 * file operation.
	 */
	public static final int SAVE_DIALOG = 1;

	/**
	 * Type value indicating that the <code>JFileChooser</code> supports a
	 * developer-specified file operation.
	 */
	public static final int CUSTOM_DIALOG = 2;

	// ********************************
	// ***** Dialog Return Values *****
	// ********************************

	/**
	 * Return value if cancel is chosen.
	 */
	public static final int CANCEL_OPTION = 1;

	/**
	 * Return value if approve (yes, ok) is chosen.
	 */
	public static final int APPROVE_OPTION = 0;

	/**
	 * Return value if an error occurred.
	 */
	public static final int ERROR_OPTION = -1;

	CustomVFS fileChooser = null;

	class CustomVFS extends VFSJFileChooser {

		public CustomVFS(File dir) {
			super(dir);
		}

		public CustomVFS() {
			super();
		}

		public void trySetDimensions(Dialog dialog) {
			APrintProperties properties = APrintProperties.getLastDefinedProperties();
			initPrefsStorage(new PrefixedNamePrefsStorage("windows_properties", //$NON-NLS-1$
					new FilePrefsStorage(new File(properties.getAprintFolder(), "aprintfilewindows.properties"))));//$NON-NLS-1$

			try {
				Dimension dimensions = prefixedNamePrefsStorage.getDimension("windowfilesimensions");
				if (dimensions != null) {
					int width = (int) Math.max(800.0, dimensions.getWidth());
					int height = (int) Math.max(500.0, dimensions.getHeight());
					dialog.setSize(new Dimension(width, height));
				}
			} catch (RuntimeException ex) {
				logger.debug("fail to set component dimension" + ex.getMessage(), ex);
			}
		}

		@Override
		protected JDialog createDialog(Component parent) throws HeadlessException {
			JDialog dialog = super.createDialog(parent);
			trySetDimensions(dialog);
			return dialog;
		}

	}

	public APrintFileChooser() {
		this.fileChooser = new CustomVFS();
		customizeFileView();
	}

	public APrintFileChooser(File currentDir) {
		if (currentDir != null) {
			this.fileChooser = new CustomVFS(currentDir);
		} else {
			this.fileChooser = new CustomVFS();
		}
		customizeFileView();
	}

	protected void customizeFileView() {
		BookmarkPanel p = new BookmarkPanel(fileChooser);
		fileChooser.setAccessory(p);
	}

	public void setSelectedFile(File selectedFile) {
		try {
			AbstractFileObject fileObject = convertToFileObject(selectedFile);
			setSelectedFile(fileObject);
		} catch (Exception ex) {
			logger.error("cannot set selected file :" + selectedFile, ex);
		}
	}

	public static AbstractFileObject convertToFileObject(File selectedFile) throws Exception {

		if (selectedFile == null) {
			return null;
		}

		return VFSTools.fromRegularFile(selectedFile);
	}

	public void setSelectedFile(AbstractFileObject selectedFile) {
		// tolerant to null
		try {
			this.fileChooser.setSelectedFileObject(selectedFile);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
	}

	public void setCurrentDirectory(File currentDirectory) {
		if (currentDirectory == null) {
			logger.warn("no current directory passed, set current directory will not be setted");
			return;
		}
		try {

			if (currentDirectory != null && currentDirectory.getParentFile() == null) {
				return;
			}

			FileObject fo = VFSTools.fromRegularFile(currentDirectory);

			this.fileChooser.setCurrentDirectoryObject(fo);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public void setFileSelectionMode(int selectionMode) {
		switch (selectionMode) {
		case FILES_ONLY:
			fileChooser.setFileSelectionMode(SELECTION_MODE.FILES_ONLY);
			break;
		case FILES_AND_DIRECTORIES:
			fileChooser.setFileSelectionMode(SELECTION_MODE.FILES_AND_DIRECTORIES);
			break;
		case DIRECTORIES_ONLY:
			fileChooser.setFileSelectionMode(SELECTION_MODE.DIRECTORIES_ONLY);
			break;
		}
	}

	public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
		fileChooser.setMultiSelectionEnabled(multiSelectionEnabled);
	}

	public void setFileFilter(VFSFileNameExtensionFilter fileFilter) {
		fileChooser.setFileFilter(fileFilter);
	}

	public void addFileFilter(VFSFileNameExtensionFilter fileFilter) {
		if (fileFilter == null) {
			logger.warn("add FileFilter called with null");
			return;
		}
		fileChooser.addChoosableFileFilter(fileFilter);
	}

	public VFSFileNameExtensionFilter getSelectedFileFilter() {
		return (VFSFileNameExtensionFilter) fileChooser.getFileFilter();
	}

	public void setDialogTitle(String title) {
		fileChooser.setDialogTitle(title);
	}

	public void setApproveButtonText(String text) {
		fileChooser.setApproveButtonText(text);
	}

	public void setApproveButtonMnemonic(char buttonMnemonic) {
		fileChooser.setApproveButtonMnemonic(buttonMnemonic);
	}

	/** Save the users preferences */
	protected PrefixedNamePrefsStorage prefixedNamePrefsStorage;

	/**
	 * This function get the internal name of the frame for preferences by default,
	 * take the name of the current class
	 *
	 * @return
	 */
	protected String getInternalFrameNameForPreferences() {
		return getClass().getSimpleName();
	}

	protected void initPrefsStorage(IPrefsStorage prefsStorage) {
		prefixedNamePrefsStorage = new PrefixedNamePrefsStorage(getInternalFrameNameForPreferences(), prefsStorage);
		try {
			prefixedNamePrefsStorage.load();
		} catch (Exception ex) {
			logger.error("error in loading prefs :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	public int showOpenDialog(Component parentComponent) {
		// tolerant to null
		RETURN_TYPE ret = fileChooser.showOpenDialog(parentComponent);
		trySaveDimensions();

		return convertToFileChooserOption(ret);
	}

	public void trySaveDimensions() {
		try {
			Dimension dimensions = fileChooser.getSize();
			prefixedNamePrefsStorage.setDimension("windowfilesimensions", dimensions);
			prefixedNamePrefsStorage.save();
		} catch (RuntimeException ex) {
			logger.debug("fail to save component dimension" + ex.getMessage(), ex);
		}
	}

	private int convertToFileChooserOption(RETURN_TYPE ret) {
		if (ret != null) {
			switch (ret) {
			case APPROVE:
				return this.APPROVE_OPTION;
			case CANCEL:
				return this.CANCEL_OPTION;
			case ERROR:
				return this.ERROR_OPTION;

			default:
				break;
			}
		}

		return this.APPROVE_OPTION;
	}

	public int showSaveDialog(Component parentComponent) {

		int retvalue = convertToFileChooserOption(fileChooser.showSaveDialog(parentComponent));
		trySaveDimensions();
		return retvalue;
	}

	public int showDialog(Component parentComponent, String label) {

		int convertToFileChooserOption = convertToFileChooserOption(fileChooser.showDialog(parentComponent, label));
		trySaveDimensions();
		return convertToFileChooserOption;
	}

	public AbstractFileObject getSelectedFile() {

		FileObject selectedFile = fileChooser.getSelectedFileObject();
		if (selectedFile == null) {
			return null;
		}

		if (selectedFile instanceof AbstractFileObject) {
			AbstractFileObject l = (AbstractFileObject) selectedFile;
			return l;
		}
		throw new RuntimeException("invalid selected file :" + selectedFile);
	}

	public FileObject[] getSelectedFiles() {
		FileObject[] selectedFiles = fileChooser.getSelectedFileObjects();
		return selectedFiles;
	}

}
