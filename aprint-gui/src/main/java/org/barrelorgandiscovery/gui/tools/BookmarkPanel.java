package org.barrelorgandiscovery.gui.tools;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import org.jdesktop.swingx.HorizontalLayout;

import com.googlecode.vfsjfilechooser2.VFSJFileChooser;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.Bookmarks;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.BookmarksDialog;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.TitledURLEntry;
import com.googlecode.vfsjfilechooser2.utils.VFSUtils;

/**
 * panel for showing the bookmarks
 * 
 * @author pfreydiere
 * 
 */
public class BookmarkPanel extends JComponent implements ActionListener {

	BookmarksDialog dialog;
	Bookmarks model;
	JTable table;
	JScrollPane scrollPane;
	Dimension tableSize = new Dimension(350, 200);
	JButton goToBookmark;
	JButton editDialog;

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

		goToBookmark = new JButton("GoTo");
		goToBookmark.addActionListener(this);

		editDialog = new JButton("Edit ...");
		editDialog.addActionListener(this);

		add(scrollPane, BorderLayout.CENTER);
		add(lbl, BorderLayout.NORTH);

		HorizontalLayout hl = new HorizontalLayout(10);
		JPanel btnPanel = new JPanel();
		btnPanel.setPreferredSize(new Dimension(30, 20));
		btnPanel.setLayout(hl);
		btnPanel.add(goToBookmark);

		btnPanel.add(editDialog);

		add(btnPanel, BorderLayout.SOUTH);

		lbl.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, UIManager.getColor("Panel.background")));
		setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, UIManager.getColor("Panel.background")));

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();

		final int row = table.getSelectedRow();

		if (button.equals(goToBookmark)) {
			if (row != NO_BOOKMARK_SELECTION_INDEX) {
				Thread worker = new Thread() {
					@Override
					public void run() {
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						TitledURLEntry aTitledURLEntry = model.getEntry(row);

						FileObject fo = null;

						try {
							fo = VFSUtils.resolveFileObject(aTitledURLEntry.getURL());

							if ((fo != null) && !fo.exists()) {
								fo = null;
							}
						} catch (Exception exc) {
							fo = null;
						}

						setCursor(Cursor.getDefaultCursor());

						if (fo == null) {
							StringBuilder msg = new StringBuilder();
							msg.append("Failed to connect to ");
							msg.append(aTitledURLEntry.getURL());
							msg.append("\n");
							msg.append("Please check URL entry and try again.");
							JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
						} else {
							chooser.setCurrentDirectory(fo);
							if (dialog != null) {
								dialog.setVisible(false);
							}
						}
					}
				};

				worker.setPriority(Thread.MIN_PRIORITY);
				SwingUtilities.invokeLater(worker);
			}

		} else if (button.equals(editDialog)) {
			if (row != NO_BOOKMARK_SELECTION_INDEX) {

				dialog.showEditorView(row);
				// dialog.showEditorView(row);
				dialog.setVisible(true);
			} else {
				dialog.restoreDefaultView();
				dialog.setVisible(true);
				
			}
		}
	}

}