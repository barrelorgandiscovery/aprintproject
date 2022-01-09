package org.barrelorgandiscovery.gui;

import java.io.File;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.ui.tools.VFSTools;

public class TestAPrintFileChooser {

	public static void main(String[] args) throws Exception 
	{
//		APrintFileChooser fc = new APrintFileChooser();
//		fc.showOpenDialog(null);
//		
		
		StandardFileSystemManager manager = VFSTools.getManager();
		APrintFileChooser fcWithNull = new APrintFileChooser(null);
		
		fcWithNull.setSelectedFile((AbstractFileObject)null);
		
		fcWithNull.setSelectedFile((File)null);
		
		fcWithNull.showOpenDialog(null);
		
		
	}
	
}
