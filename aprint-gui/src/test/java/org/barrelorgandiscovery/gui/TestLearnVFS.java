package org.barrelorgandiscovery.gui;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.webdav.WebdavFileProvider;
import org.apache.commons.vfs2.provider.webdav4s.Webdav4sFileProvider;
import org.barrelorgandiscovery.ui.tools.VFSTools;
import org.junit.Test;

public class TestLearnVFS {

	
	
	// @Test
	public void testConvertToFile() throws Exception {
		
		
		FileObject fo = VFS.getManager().resolveFile("c:\\");
		System.out.println(fo);
		assert fo.exists();
		
		File f = VFSTools.convertToFile(fo);
		assert f.exists();
		
		System.out.println(f);
	}
	
	@Test
	public void testAccessWebDav() throws Exception {
		
		
		FileSystemManager fsManager = VFS.getManager();
		System.out.println(fsManager);
		StandardFileSystemManager sm = (StandardFileSystemManager)fsManager;
		sm.addProvider("webdav", new Webdav4sFileProvider());
			
		FileObject fo = VFS.getManager().resolveFile("webdav://localhost/test/testdir");
		//FileObject fo = VFS.getManager().resolveFile("https://www.barrel-organ-discovery.org/builds/2015/");
		
		
		System.out.println(fo);
		assert fo.exists();
		
		FileObject[] children = fo.getChildren();
		for ( int i = 0; i < children.length; i++ ){
		    System.out.println( children[ i ].getName().getBaseName() );
		}
	}
	

	@Test
	public void testFtp() throws Exception {
		
		FileObject fo = VFS.getManager().resolveFile("ftp://fstorage.frett27.net");
		
		System.out.println(fo);
		assert fo.exists();
		
		FileObject[] children = fo.getChildren();
		for ( int i = 0; i < children.length; i++ ){
		    System.out.println( children[ i ].getName().getBaseName() );
		}
	}


	@Test
	public void testsFtp() throws Exception {
		
		FileObject fo = VFS.getManager().resolveFile("sftp://fstorage.frett27.net");
		
		System.out.println(fo);
		assert fo.exists();
		
		FileObject[] children = fo.getChildren();
		for ( int i = 0; i < children.length; i++ ){
		    System.out.println( children[ i ].getName().getBaseName() );
		}
	}

	
}
