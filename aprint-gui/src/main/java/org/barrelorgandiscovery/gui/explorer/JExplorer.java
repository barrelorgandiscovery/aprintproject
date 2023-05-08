package org.barrelorgandiscovery.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.VFSTools;

import com.googlecode.vfsjfilechooser2.accessories.bookmarks.Bookmarks;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.TitledURLEntry;

/**
 * explorer component
 * 
 * @author pfreydiere
 *
 */
public class JExplorer extends JPanel implements Explorer {

	private static Logger logger = Logger.getLogger(JExplorer.class);

	private Vector<ExplorerListener> listeners = new Vector<>();

	/**
	 * loadable node
	 *
	 */
	public abstract class ExpNode extends DefaultMutableTreeNode {

		protected boolean loaded;

		public boolean isLoaded() {
			return loaded;
		}

		public abstract void load() throws Exception;

		public abstract boolean reload() throws Exception;
		
	}

	public class ExpFFolderNode extends ExpNode {

		private FileObject fo;
		private String unconnected;

		public ExpFFolderNode(String unconnected) {
			this.unconnected = unconnected;
			this.userObject = unconnected;
			DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode("not connected ... ");
			add(defaultMutableTreeNode);

		}

		public ExpFFolderNode(FileObject fo) {
			this.fo = fo;
			this.userObject = fo.getName().getBaseName();
			setAllowsChildren(true);
			DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode("Loading ... ");
			add(defaultMutableTreeNode);
		}
		
		public ExpFFolderNode(FileObject fo, String overloadedName) {
			this(fo);
			this.userObject = overloadedName;
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
		public boolean isLeaf() {
			try {
				if (fo == null && unconnected != null) {
					return false;
				}
				return fo.isFile();
			} catch (Exception ex) {
				// throw new RuntimeException(ex.getMessage(), ex);
				return false;
			}
		}

		@Override
		public void load() throws Exception {
			if (loaded) {
				return;
			}

			if (fo == null && unconnected != null) {
				// connecting
				FileObject resolveFile = VFSTools.getManager().resolveFile(unconnected);
				fo = resolveFile;
			}

			if (!reload()) {
				this.removeAllChildren();
				((DefaultTreeModel) JExplorer.this.tree.getModel()).nodeStructureChanged(this);
			}
			
			loaded = true;
		}

		public boolean reload() throws FileSystemException {
			this.removeAllChildren();
			FileObject[] childrens = fo.getChildren();
			
			boolean retvalue = false;
			if (childrens != null) {
				// sort the filename
				Arrays.sort(childrens, new Comparator<FileObject>() {
					public int compare(FileObject o1, FileObject o2) {
						return o1.getName().getBaseName().compareTo(o2.getName().getBaseName());
					};
				});

				for (FileObject f : childrens) {

					add(new ExpFFolderNode(f));
					((DefaultTreeModel) JExplorer.this.tree.getModel()).nodeStructureChanged(this);
					retvalue = true; // has children
				}

			}
			return retvalue;
		}
	}

	/**
	 * bookmark node, listing the bookmarks
	 * 
	 * @author pfreydiere
	 *
	 */
	public class ExpBookmarksNode extends ExpNode {

		private Bookmarks bookMarks;

		public ExpBookmarksNode(Bookmarks bookMarks) throws Exception {
			this.bookMarks = bookMarks;
			reload();
		}

		@Override
		public void load() throws Exception {
			loaded = true;
		}
		
		@Override
		public boolean reload() throws Exception {
			removeAllChildren();
			boolean retvalue = false;
			for (int i = 0; i < bookMarks.getSize(); i++) {
				TitledURLEntry entry = bookMarks.getEntry(i);
				String url = entry.getURL();
				try {
					FileObject resolveFile = VFSTools.getManager().resolveFile(url);
					add(new ExpFFolderNode(resolveFile, entry.getTitle()));
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					add(new ExpFFolderNode(url));
				}
				retvalue = true;
			}
			return retvalue;
		}
		
	}

	protected JTree tree;

	private Object owner;
	
	/**
	 * constructor
	 * 
	 * @throws Exception
	 */
	public JExplorer(Object owner) throws Exception {
		super();
		this.owner = owner;
		setLayout(new BorderLayout());
		tree = new JTree(new ExpBookmarksNode(new Bookmarks()));
		add(tree, BorderLayout.CENTER);
		tree.addTreeExpansionListener(new TreeExpansionListener() {

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				DefaultMutableTreeNode node;
				node = (DefaultMutableTreeNode) (event.getPath().getLastPathComponent());
				try {
					((ExpNode) node).load();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}

			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {

			}
		});

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {

				TreePath[] selectionPaths = tree.getSelectionModel().getSelectionPaths();
				List<FileObject> selected = new ArrayList<>();
				if (selectionPaths != null) {
					for (TreePath tp : selectionPaths) {
						if (tp == null) {
							continue;
						}
						if (tp.getLastPathComponent() instanceof ExpFFolderNode) {
							ExpFFolderNode fon = (ExpFFolderNode) tp.getLastPathComponent();
							selected.add(fon.fo);
						}
					}
				}

				for (ExplorerListener l : listeners) {
					try {
						l.selectionChanged(selected.toArray(new FileObject[0]));
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			}
		});

		tree.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 1) {
						// mySingleClick(selRow, selPath);
					} else if (e.getClickCount() == 2) {
						for (ExplorerListener l : listeners) {
							try {
								l.doubleClick(((ExpFFolderNode) selPath.getLastPathComponent()).fo);
							} catch (Exception ex) {
								logger.error(ex.getMessage(), ex);
							}
						}
					}
				}
			}

		});

	}

	public void reload() throws Exception {
		ExpBookmarksNode root = new ExpBookmarksNode(new Bookmarks());
		tree.setModel(new DefaultTreeModel(root));
		root.reload();
	}
	
	@Override
	public void addExplorerListener(ExplorerListener listener) {
		if (listener != null)
			listeners.add(listener);
	}

	@Override
	public void removeExplorerListener(ExplorerListener listener) {
		listeners.remove(listener);
	}

	public static void main(String[] args) throws Exception {
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new JExplorer(null), BorderLayout.CENTER);
		f.setSize(new Dimension(700, 500));
		f.setVisible(true);
	}
}
