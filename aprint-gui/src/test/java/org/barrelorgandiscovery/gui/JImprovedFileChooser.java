package org.barrelorgandiscovery.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class JImprovedFileChooser extends JFileChooser {

	public JImprovedFileChooser() {
		super();
	}

	public JImprovedFileChooser(File currentDirectory, FileSystemView fsv) {
		super(currentDirectory, fsv);
	}

	public JImprovedFileChooser(File currentDirectory) {
		super(currentDirectory);
	}

	public JImprovedFileChooser(FileSystemView fsv) {
		super(fsv);
	}

	public JImprovedFileChooser(String currentDirectoryPath, FileSystemView fsv) {
		super(currentDirectoryPath, fsv);
	}

	public JImprovedFileChooser(String currentDirectoryPath) {
		super(currentDirectoryPath);
	}

	
	
}
