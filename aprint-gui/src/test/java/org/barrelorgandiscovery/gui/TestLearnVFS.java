package org.barrelorgandiscovery.gui;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.barrelorgandiscovery.tools.VFSTools;
import org.barrelorgandiscovery.vfs2.provider.BodProvider;
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

		FileObject fo = VFSTools.getManager().resolveFile("webdav://localhost:80/");
		// FileObject fo =
		// VFS.getManager().resolveFile("https://www.barrel-organ-discovery.org/builds/2015/");

		System.out.println(fo);
		assert fo.exists();

		FileObject[] children = fo.getChildren();
		for (int i = 0; i < children.length; i++) {
			System.out.println(children[i].getName().getBaseName());
		}
	}

	@Test
	public void testFtp() throws Exception {

		FileObject fo = VFSTools.getManager().resolveFile("ftp://fstorage.frett27.net");

		System.out.println(fo);
		assert fo.exists();

		FileObject[] children = fo.getChildren();
		for (int i = 0; i < children.length; i++) {
			System.out.println(children[i].getName().getBaseName());
		}
	}

	@Test
	public void testsFtp() throws Exception {

		FileObject fo = VFSTools.getManager().resolveFile("sftp://fstorage.frett27.net");

		System.out.println(fo);
		assert fo.exists();

		FileObject[] children = fo.getChildren();
		for (int i = 0; i < children.length; i++) {
			System.out.println(children[i].getName().getBaseName());
		}
	}

	@Test
	public void testBodProvider() throws Exception {
//		System.out.println("add provider");
//		VFSTools.getManager().addProvider("bod", new BodProvider());
		System.out.println("resolve file");
		FileObject fo = VFSTools.getManager().resolveFile("bod://localhost:80/test");
		// FileObject fo =
		// VFS.getManager().resolveFile("https://www.barrel-organ-discovery.org/builds/2015/");

		System.out.println(fo);
		assert fo.exists();

		FileObject[] children = fo.getChildren();
		for (int i = 0; i < children.length; i++) {
			System.out.println(children[i].getName().getBaseName());
			System.out.println(children[i].isFolder());
		}
	}

}
