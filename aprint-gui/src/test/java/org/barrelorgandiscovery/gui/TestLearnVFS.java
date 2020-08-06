package org.barrelorgandiscovery.gui;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.barrelorgandiscovery.ui.tools.VFSTools;
import org.junit.Test;

public class TestLearnVFS {

	
	
	@Test
	public void testConvertToFile() throws Exception {
		
		
		FileObject fo = VFS.getManager().resolveFile("c:\\");
		System.out.println(fo);
		assert fo.exists();
		
		
		File f = VFSTools.convertToFile(fo);
		assert f.exists();
		
		System.out.println(f);
	}
	
}
