package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.search.ISearchPanelListener;
import org.barrelorgandiscovery.gui.search.SearchPanel;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.search.BookIndexing;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.VFSTools;

public class APrintNGSearchInternalFrame extends APrintNGInternalFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8736227553831150386L;

	private static Logger logger = Logger.getLogger(APrintNGSearchInternalFrame.class);

	private APrintProperties props;
	private BookIndexing bookIndexing;
	private APrintNG services;

	private SearchPanel sp;

	public APrintNGSearchInternalFrame(APrintProperties props, APrintNG services, BookIndexing bookIndexing)
			throws Exception {

		super(props.getFilePrefsStorage(), Messages.getString("APrintNGSearchInternalFrame.0"), true, true, true, true); //$NON-NLS-1$

		this.props = props;
		this.bookIndexing = bookIndexing;
		this.services = services;

		initComponents();
	}

	private void initComponents() throws Exception {

		sp = new SearchPanel(bookIndexing, props, this, services.getOwnerForDialog());

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(sp, BorderLayout.CENTER);

		JButton open = new JButton(Messages.getString("APrintNGSearchInternalFrame.1")); //$NON-NLS-1$
		open.setHorizontalAlignment(SwingConstants.RIGHT);

		JButton export = new JButton("Export result list to file ...");
		export.addActionListener((e) -> {
			try {

				TableModel model = sp.getLatestResults();
				// output to a file
				APrintFileChooser f = new APrintFileChooser();
				f.addFileFilter(new VFSFileNameExtensionFilter("Excel file", "xlsx"));
				if (f.showSaveDialog(this) == APrintFileChooser.APPROVE_OPTION) {
					AbstractFileObject selectedFile = f.getSelectedFile();
					logger.debug("saving in " + selectedFile);
					OutputStream outputStream = VFSTools.transactionalWrite(selectedFile);
					try {
						// create a new workbook
						Workbook wb = new HSSFWorkbook();

						// create a new sheet
						Sheet s = wb.createSheet();

						int columnCount = model.getColumnCount();
						int rowCount = model.getRowCount();

						int rownum = 0;
						{
							// create header
							Row r = s.createRow(rownum);
							for (int col = 0; col < columnCount; col++) {
								Cell c = r.createCell(col);
								c.setCellValue(model.getColumnName(col));
							}
						}
						rownum++;

						for (int num = 0; num < rowCount; num++, rownum++) {

							// create a row
							Row r = s.createRow(rownum);
							for (short cellnum = (short) 0; cellnum < columnCount; cellnum++) {

								// create a numeric cell
								Cell c = r.createCell(cellnum);

								Object v = model.getValueAt(num, cellnum);

								// don't export nulls
								String sValue = "";
								if (v != null) {
									sValue = "" + v;
								}
						
								c.setCellValue(sValue);

							}
						}

						wb.write(outputStream);

					} finally {
						outputStream.close();
					}

				}

			} catch (Exception ex) {
				logger.error("error while searching ... " + ex.getMessage(), ex); //$NON-NLS-1$
				JMessageBox.showMessage(services.getOwnerForDialog(),
						Messages.getString("APrintNGSearchInternalFrame.3") + ex.getMessage()); //$NON-NLS-1$
			}

		});

		JPanel p = new JPanel();
		p.add(open);
		p.add(export);

		getContentPane().add(p, BorderLayout.SOUTH);

		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					openSelectedElement();

				} catch (Throwable ex) {
					logger.error("error while searching ... " + ex.getMessage(), ex); //$NON-NLS-1$
					JMessageBox.showError(this,
							ex); //$NON-NLS-1$
				}
			}
		});

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

}
