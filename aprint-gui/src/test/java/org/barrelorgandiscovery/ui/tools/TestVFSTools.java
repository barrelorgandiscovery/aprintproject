package org.barrelorgandiscovery.ui.tools;

import java.io.File;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.barrelorgandiscovery.tools.VFSTools;
import org.junit.jupiter.api.Test;

public class TestVFSTools {

	@Test
	public void testVFSTools() throws Exception {
		
		File f = new File("/home/use/Téléchargements/16_la_coquette___dolcine_16 notes -325884608.instrumentbundle");
		assert f.exists();
		
		AbstractFileObject fromRegularFile = VFSTools.fromRegularFile(f);
		System.out.println(fromRegularFile);
		
	}
	
}
