package org.barrelorgandiscovery.gui;

import org.barrelorgandiscovery.gui.tools.APrintFileChooser;

public class TestDirectorySelection {
	
	public static void main(String[] main) throws Exception {
		
		APrintFileChooser aPrintFileChooser = new APrintFileChooser();
		aPrintFileChooser.setFileSelectionMode(APrintFileChooser.DIRECTORIES_ONLY);
		
		aPrintFileChooser.showOpenDialog(null);
		System.out.println(aPrintFileChooser.getSelectedFile());
	}
	
}
