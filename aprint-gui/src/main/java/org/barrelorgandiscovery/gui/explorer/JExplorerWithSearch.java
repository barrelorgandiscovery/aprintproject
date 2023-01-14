package org.barrelorgandiscovery.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.search.ISearchPanelListener;
import org.barrelorgandiscovery.gui.search.SearchPanel;
import org.barrelorgandiscovery.search.BookIndexing;
import org.barrelorgandiscovery.tools.JMessageBox;

import com.googlecode.vfsjfilechooser2.accessories.bookmarks.BookmarksDialog;

public class JExplorerWithSearch extends JPanel {

	private static final long serialVersionUID = 6815215661644426802L;

	private static Logger logger = Logger.getLogger(JExplorerWithSearch.class);

	private APrintProperties props;
	private BookIndexing bookIndexing;
	private APrintNG services;

	private SearchPanel sp;

	private JExplorer explorer;

	public JExplorerWithSearch(APrintProperties props, APrintNG services, BookIndexing bookIndexing) throws Exception {
		super();
		this.props = props;
		this.bookIndexing = bookIndexing;
		this.services = services;
		initComponents();
	}

	protected void initComponents() throws Exception {
		setLayout(new BorderLayout());
		explorer = new JExplorer();

		JPanel explorerPanelWithTools = new JPanel();
		explorerPanelWithTools.setLayout(new BorderLayout());
		explorerPanelWithTools.add(explorer, BorderLayout.CENTER);

		// tools on top
		JToolBar tb = new JToolBar();
		explorerPanelWithTools.add(tb, BorderLayout.NORTH);
		JButton bookmarkButton = new JButton("BookMarks ...");
		tb.add(bookmarkButton);
		bookmarkButton.addActionListener((e) -> {
			try {
				BookmarksDialog bookmarkDialog = new BookmarksDialog((Frame) services.getOwner(), null);
				bookmarkDialog.setVisible(true);
				explorer.reload();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});

		sp = new SearchPanel(bookIndexing, props, services, this);

		JSplitPane spane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(explorerPanelWithTools), sp);
		add(spane, BorderLayout.CENTER);

		sp.setSearchPanelListener(new ISearchPanelListener() {
			public void mouseDblClickOnElement() {
				try {

					openSelectedElement();

				} catch (Throwable ex) {
					logger.error("error while searching ... " + ex.getMessage(), ex); //$NON-NLS-1$
					JMessageBox.showError(this, ex);
				}
			}
		});

	}

	private void openSelectedElement() throws Exception, FileNotFoundException {
		URL[] selectedItems = sp.getSelectedItems();
		for (int i = 0; i < selectedItems.length; i++) {
			URL url = selectedItems[i];

			String path = url.getPath();
			File bookFile = new File(path);

			if (bookFile.exists()) {
				services.loadBookInNewFrame(bookFile);
			}
		}
	}

	public void addExplorerListener(ExplorerListener listener) {
		explorer.addExplorerListener(listener);
	}

	public void removeExplorerListener(ExplorerListener listener) {
		explorer.removeExplorerListener(listener);
	}

}
