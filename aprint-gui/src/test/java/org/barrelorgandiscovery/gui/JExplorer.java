package org.barrelorgandiscovery.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.vfs2.FileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.VFSTools;

import com.googlecode.vfsjfilechooser2.accessories.bookmarks.Bookmarks;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.TitledURLEntry;

public class JExplorer extends JPanel {

	private static Logger logger = Logger.getLogger(JExplorer.class);

	public abstract class ExpNode extends DefaultMutableTreeNode {

		protected boolean loaded;

		public boolean isLoaded() {
			return loaded;
		}

		public abstract void load() throws Exception;

	}

	public class ExpFFolderNode extends ExpNode {

		private FileObject fo;

		public ExpFFolderNode(FileObject fo) {
			this.fo = fo;
			this.userObject = fo;
			setAllowsChildren(true);
			DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode("Loading");
			add(defaultMutableTreeNode);
		}

	
		
		private boolean _selected;

		public void setSelected(boolean s) {
			if (s != _selected) {
				_selected = s;
			}
		}

		public boolean isSelected() {
			return _selected;
		}

		@Override
		public void load() throws Exception {
			this.removeAllChildren();
			FileObject[] childrens = fo.getChildren();
			if (childrens != null) {
				for (FileObject f : childrens) {
					add(new ExpFFolderNode(f));
				}
			}
			loaded = true;

		}
	}

	public class ExpBookmarksNode extends ExpNode {

		private Bookmarks bookMarks;

		public ExpBookmarksNode(Bookmarks bookMarks) throws Exception {
			this.bookMarks = bookMarks;
			for (int i = 0; i < bookMarks.getSize(); i++) {
				TitledURLEntry entry = bookMarks.getEntry(i);
				String url = entry.getURL();
				try {
					FileObject resolveFile = VFSTools.getManager().resolveFile(url);
					add(new ExpFFolderNode(resolveFile));
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}

		@Override
		public void load() throws Exception {
			loaded = true;
		}

	}

	protected JTree tree;

	public JExplorer() throws Exception {
		super();

		setLayout(new BorderLayout());
		tree = new JTree(new ExpBookmarksNode(new Bookmarks()));
		add(tree, BorderLayout.CENTER);
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				Object source = event.getSource();
				if (source instanceof ExpFFolderNode) {
					ExpFFolderNode f = (ExpFFolderNode)source;
					if (!f.loaded) {
						try {
						f.load();
						} catch(Exception ex) {
							logger.error(ex.getMessage(), ex);
						}
					}
				}
				
			}
			
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				// TODO Auto-generated method stub
				
			}
		});

	}

	public static void main(String[] args) throws Exception {

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new JExplorer(), BorderLayout.CENTER);
		f.setSize(new Dimension(700, 500));
		f.setVisible(true);
	}
}
