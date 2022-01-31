package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.search.ISearchPanelListener;
import org.barrelorgandiscovery.gui.search.SearchPanel;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.search.BookIndexing;
import org.barrelorgandiscovery.tools.JMessageBox;

public class APrintNGSearchInternalFrame extends APrintNGInternalFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8736227553831150386L;

	private static Logger logger = Logger
			.getLogger(APrintNGSearchInternalFrame.class);

	private APrintProperties props;
	private BookIndexing bookIndexing;
	private APrintNG services;
	

	private SearchPanel sp;

	public APrintNGSearchInternalFrame(APrintProperties props,
			APrintNG services, BookIndexing bookIndexing) throws Exception {

		super(props.getFilePrefsStorage(),
				Messages.getString("APrintNGSearchInternalFrame.0"), true, true, true, true); //$NON-NLS-1$

		this.props = props;
		this.bookIndexing = bookIndexing;
		this.services = services;
		

		initComponents();
	}

	private void initComponents() throws Exception {

		sp = new SearchPanel(bookIndexing, props, this,
				services.getOwnerForDialog());

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(sp, BorderLayout.CENTER);

		JButton open = new JButton(
				Messages.getString("APrintNGSearchInternalFrame.1")); //$NON-NLS-1$
		open.setHorizontalAlignment(SwingConstants.RIGHT);

		JPanel p = new JPanel();
		p.add(open);

		getContentPane().add(p, BorderLayout.SOUTH);

		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					
					openSelectedElement();

				} catch (Exception ex) {
					logger.error(
							"error while searching ... " + ex.getMessage(), ex); //$NON-NLS-1$
					JMessageBox.showMessage(
							services.getOwnerForDialog(),
							Messages.getString("APrintNGSearchInternalFrame.3") + ex.getMessage()); //$NON-NLS-1$
				}
			}
		});

		sp.setSearchPanelListener(new ISearchPanelListener() {
			public void mouseDblClickOnElement() {
				try {

					openSelectedElement();

				} catch (Exception ex) {
					logger.error(
							"error while searching ... " + ex.getMessage(), ex); //$NON-NLS-1$
					JMessageBox.showError(services.getOwnerForDialog(), ex);
//					JMessageBox.showMessage(
//							services.getOwnerForDialog(),
//							Messages.getString("APrintNGSearchInternalFrame.3") + ex.getMessage()); //$NON-NLS-1$
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

}
