package org.barrelorgandiscovery.gui;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileObject;

public class TestImprovedFileChooser {

	// @Test
	public void testFileObject() throws Exception {

		FileObject f = VFS.getManager().resolveFile("c:\\temp");
		System.out.println(f);

		File file = new File("c:\\temp");
		FileObject fromFile = VFS.getManager().resolveFile(file.getAbsolutePath());

		System.out.println(fromFile);

		System.out.println(fromFile instanceof AbstractFileObject);
		AbstractFileObject a = (AbstractFileObject) fromFile;

	}
//
//	public static void main(String[] args) throws Exception {
//
//		FileSystemManager fsManager = VFS.getManager();
//		System.out.println(fsManager);
//		StandardFileSystemManager sm = (StandardFileSystemManager) fsManager;
//		sm.addProvider("webdav", new Webdav4sFileProvider());
//
//		JFrame frame = new JFrame();
//		JButton btn = new JButton("open");
//
//		frame.getContentPane().add(btn);
//
//		btn.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				try {
//					VFSJFileChooser fileChooser = new VFSJFileChooser(
//							VFS.getManager().resolveFile("webdav://localhost/test/testdir/"));
//					// final Frame ancestor = (Frame) SwingUtilities.getWindowAncestor(fileChooser);
//					BookmarksDialog dialog = new BookmarksDialog(null, fileChooser);
//
//					BookmarkPanel p = new BookmarkPanel(fileChooser);
//
//					DefaultAccessoriesPanel d = new DefaultAccessoriesPanel(fileChooser);
//					fileChooser.setAccessory(p);
//
//					RETURN_TYPE r = fileChooser.showSaveDialog(null);
//
//					FileObject selectedFile = fileChooser.getSelectedFileObject();
//					if (selectedFile instanceof AbstractFileObject) {
//						AbstractFileObject f = (AbstractFileObject) selectedFile;
//						System.out.println("is list children capable ?"
//								+ f.getFileSystem().hasCapability(Capability.LIST_CHILDREN));
//					}
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//		});
//
//		frame.setSize(200, 300);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setVisible(true);
//	}

}
