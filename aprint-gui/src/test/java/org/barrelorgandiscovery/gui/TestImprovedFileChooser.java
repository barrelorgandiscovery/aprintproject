package org.barrelorgandiscovery.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.barrelorgandiscovery.gui.tools.BookmarkPanel;
import org.junit.Test;

import com.googlecode.vfsjfilechooser2.VFSJFileChooser;
import com.googlecode.vfsjfilechooser2.VFSJFileChooser.RETURN_TYPE;
import com.googlecode.vfsjfilechooser2.accessories.DefaultAccessoriesPanel;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.BookmarksDialog;

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

	public static void main(String[] args) {

		
		JFrame frame = new JFrame();
		JButton btn = new JButton("open");
		
		frame.getContentPane().add(btn);
		
		
		
		btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				VFSJFileChooser fileChooser = new VFSJFileChooser();
				 //  final Frame ancestor = (Frame) SwingUtilities.getWindowAncestor(fileChooser);
				BookmarksDialog dialog = new BookmarksDialog(null, fileChooser);

				BookmarkPanel p = new BookmarkPanel(fileChooser);

				DefaultAccessoriesPanel d = new DefaultAccessoriesPanel(fileChooser);
				fileChooser.setAccessory(p);

				RETURN_TYPE r = fileChooser.showSaveDialog(null);

				FileObject selectedFile = fileChooser.getSelectedFile();
				if (selectedFile instanceof AbstractFileObject) {
					AbstractFileObject f = (AbstractFileObject) selectedFile;
					System.out
							.println("is list children capable ?" + f.getFileSystem().hasCapability(Capability.LIST_CHILDREN));
				}
			}
		});
		
		frame.setSize(200, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
