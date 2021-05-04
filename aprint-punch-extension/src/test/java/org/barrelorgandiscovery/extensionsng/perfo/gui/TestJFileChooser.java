package org.barrelorgandiscovery.extensionsng.perfo.gui;

import javax.swing.JFileChooser;

import org.barrelorgandiscovery.tools.FileNameExtensionFilter;

/**
 * class for testing file chooser, and evaluate the choosen file type
 * @author pfreydiere
 *
 */
public class TestJFileChooser {

	public static void main(String[] args) throws Exception {
		
		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("dxf", "dxf"));
		jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("svg", "svg"));
			
		int result = jFileChooser.showSaveDialog(null);
		
		System.out.println("result : " + result);
		System.out.println(jFileChooser.getSelectedFile());
		System.out.println(jFileChooser.getFileFilter());
		
	}
	
}
