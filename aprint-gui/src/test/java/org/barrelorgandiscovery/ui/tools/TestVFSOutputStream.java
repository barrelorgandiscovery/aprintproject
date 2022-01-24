package org.barrelorgandiscovery.ui.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.junit.Test;

public class TestVFSOutputStream {

	
	
	@Test
	public void testOutputStream() throws Exception {
		
		
		File c = File.createTempFile("tttt", "tt");
		FileOutputStream fos = new FileOutputStream(c);
		fos.write("123".getBytes());
		fos.close();
		
		AbstractFileObject v = VFSTools.fromRegularFile(c);
		
		OutputStream s = VFSTools.transactionalWrite(v);
		
		s.write("AB".getBytes());
		s.flush();
		s.close();
		
		System.out.println(c);
		System.out.println(c.length());
	}
	
	
}
