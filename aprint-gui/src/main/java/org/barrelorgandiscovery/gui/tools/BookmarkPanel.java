package org.barrelorgandiscovery.gui.tools;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.jdesktop.swingx.HorizontalLayout;

import com.googlecode.vfsjfilechooser2.VFSJFileChooser;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.Bookmarks;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.BookmarksDialog;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.TitledURLEntry;

/**
 * panel for showing the bookmarks
 * 
 * @author pfreydiere
 * 
 */
public class BookmarkPanel extends JComponent implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7649555599019379378L;
	
	BookmarksDialog dialog;
	Bookmarks model;
	JTable table;
	JScrollPane scrollPane;

	Dimension tableSize = new Dimension(350, 200);

	JButton goToBookmark;

	JButton editDialog;

	JButton addNewBookmark;

	JButton removeBookmark;

	VFSJFileChooser chooser;

	public static final int NO_BOOKMARK_SELECTION_INDEX = -1;

	public BookmarkPanel(VFSJFileChooser chooser) {
		this.chooser = chooser;
		initComponents();
	}

	protected void initComponents() {

		setLayout(new BorderLayout());

		dialog = new BookmarksDialog(null, chooser);

		model = dialog.getBookmarks();

		JLabel lbl = new JLabel("Bookmarks");

		table = new JTable(model);

		scrollPane = new JScrollPane(table);

		table.setPreferredScrollableViewportSize(tableSize);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// double click on bookmark selection
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					final int row = table.getSelectedRow();
					gotoSelectedBookMark(row);
				}
			}
		});

		goToBookmark = new JButton("GoTo");
		goToBookmark.addActionListener(this);

		addNewBookmark = new JButton("Add new ...");
		addNewBookmark.addActionListener(this);

		editDialog = new JButton("Edit ...");
		editDialog.addActionListener(this);

		removeBookmark = new JButton("Remove ...");
		removeBookmark.addActionListener(this);

		add(scrollPane, BorderLayout.CENTER);
		add(lbl, BorderLayout.NORTH);

		HorizontalLayout hl = new HorizontalLayout(10);
		JPanel btnPanel = new JPanel();
		btnPanel.setPreferredSize(new Dimension(30, 20));
		btnPanel.setLayout(hl);

		btnPanel.add(goToBookmark);
		btnPanel.add(editDialog);
		btnPanel.add(addNewBookmark);
		btnPanel.add(removeBookmark);

		add(btnPanel, BorderLayout.SOUTH);

		lbl.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, UIManager.getColor("Panel.background")));
		setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, UIManager.getColor("Panel.background")));

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();

		final int row = table.getSelectedRow();

		if (button.equals(goToBookmark)) {
			gotoSelectedBookMark(row);

		} else if (button.equals(editDialog)) {
			if (row != NO_BOOKMARK_SELECTION_INDEX) {

				dialog.showEditorView(row);
				// dialog.showEditorView(row);
				dialog.setVisible(true);
			} else {
				dialog.restoreDefaultView();
				dialog.setVisible(true);

			}
		} else if (button.equals(addNewBookmark)) {

			String name = JOptionPane.showInputDialog("Bookmark Name");
			if (name != null && !"".equals(name)) {
				try {
					FileObject currentDirectory = chooser.getCurrentDirectoryObject();
					URL url = currentDirectory.getURL();
					TitledURLEntry entry = new TitledURLEntry(name, url.toString());
					model.add(entry);
					model.save();
					model.fireTableDataChanged();

				} catch (Exception ex) {
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}

		} else if (button.equals(removeBookmark)) {
			if (row != NO_BOOKMARK_SELECTION_INDEX) {
				TitledURLEntry aTitledURLEntry = model.getEntry(row);

				int showConfirmDialog = JOptionPane.showConfirmDialog(chooser,
						"Suppress bookmark " + aTitledURLEntry + " ?");
				if (showConfirmDialog == JOptionPane.YES_OPTION) {
					try {
						model.delete(row);
						model.save();
						model.fireTableDataChanged();

					} catch (Exception ex) {
						throw new RuntimeException(ex.getMessage(), ex);
					}
				}
			}
		}
	}

	private void gotoSelectedBookMark(final int row) {
		if (row != NO_BOOKMARK_SELECTION_INDEX) {
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					TitledURLEntry aTitledURLEntry = model.getEntry(row);

					FileObject fo = null;
					String failureMessage = null;
					try {
//						if(aTitledURLEntry.getURL().startsWith("ftp")) {
//							 FileSystemOptions opts = new FileSystemOptions();
//
//						        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
//						        FileSystemManager manager = VFS.getManager();
//fo = manager.resolveFile(aTitledURLEntry.getURL(), opts);
//						} else {
//							
						
						fo = VFS.getManager().resolveFile(aTitledURLEntry.getURL());
						//}

						if ((fo != null) && !fo.exists()) {
							fo = null;
						}
					} catch (Exception exc) {
						fo = null;
						failureMessage = exc.getMessage();
					}

					setCursor(Cursor.getDefaultCursor());

					if (fo == null) {
						StringBuilder msg = new StringBuilder();
						msg.append("Failed to connect to ");
						msg.append(aTitledURLEntry.getURL());
						msg.append("\n");
						msg.append(failureMessage).append("\n");
						msg.append("Please check URL entry and try again.");
						JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						chooser.setCurrentDirectoryObject(fo);
						if (dialog != null) {
							dialog.setVisible(false);
						}
					}
				}
			};

			// worker.setPriority(Thread.MIN_PRIORITY);
			SwingUtilities.invokeLater(worker);
		}
	}

}
