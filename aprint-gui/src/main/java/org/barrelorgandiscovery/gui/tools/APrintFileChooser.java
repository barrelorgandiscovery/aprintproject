package org.barrelorgandiscovery.gui.tools;

import java.awt.Component;
import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileObject;

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

	VFSJFileChooser fileChooser = new VFSJFileChooser();

	public APrintFileChooser() {
		this.fileChooser = new VFSJFileChooser();
		customizeFileView();
	}

	public APrintFileChooser(File currentDir) {
		this.fileChooser = new VFSJFileChooser(currentDir);
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
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static AbstractFileObject convertToFileObject(File selectedFile) throws FileSystemException {
		FileSystemManager fsManager = VFS.getManager();
		AbstractFileObject fileObject = (AbstractFileObject) fsManager.resolveFile(selectedFile.getParentFile(),
				selectedFile.getName());
		return fileObject;
	}

	public void setSelectedFile(AbstractFileObject selectedFile) {
		this.fileChooser.setSelectedFile(selectedFile);
	}

	public void setCurrentDirectory(File currentDirectory) {
		try {
			FileSystemManager fsManager = VFS.getManager();
			FileObject fileObject = fsManager.resolveFile(currentDirectory.getParentFile(), currentDirectory.getName());

			this.fileChooser.setCurrentDirectory(fileObject);
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
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

	public int showOpenDialog(Component parentComponent) {

		RETURN_TYPE ret = fileChooser.showOpenDialog(parentComponent);
		return convertToFileChooserOption(ret);
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
		return convertToFileChooserOption(fileChooser.showSaveDialog(parentComponent));
	}

	public int showDialog(Component parentComponent, String label) {
		return convertToFileChooserOption(fileChooser.showDialog(parentComponent, label));
	}

	public AbstractFileObject getSelectedFile() {

		FileObject selectedFile = fileChooser.getSelectedFile();
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
		FileObject[] selectedFiles = fileChooser.getSelectedFiles();
		return selectedFiles;
	}

}