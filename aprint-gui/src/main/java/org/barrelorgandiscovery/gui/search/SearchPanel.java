package org.barrelorgandiscovery.gui.search;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.DateFormatter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.lucene.document.Document;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.ProgressIndicator;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.search.BookIndexing;
import org.barrelorgandiscovery.search.ScoredDocument;
import org.barrelorgandiscovery.tools.JMessageBox;

import com.jeta.forms.components.panel.FormPanel;
import com.l2fprod.common.swing.JDirectoryChooser;

public class SearchPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3583311358643686874L;

	private static final Logger logger = Logger.getLogger(SearchPanel.class);

	private BookIndexing bi = null;

	private APrintProperties props = null;

	private JLabel foldervaluelabel;

	private JButton changeFolder;

	private JButton indexfolder;

	private JTextField searchfield;

	private JButton searchbutton;

	private JTable searchcomponent;

	private JLabel resultnumberlabel;

	private JTextField nameFilterField;

	private JTextField instrumentFilterField;

	private JTextField authorFilterField;

	private JTextField genderFilterField;

	private JTextField descriptionFilterField;

	private JTextField arrangerFilterField;

	private JCheckBox chkboxExclusive;

	private JTextField scaleFilterField;

	private IAPrintWait waitInterface;

	private Object owner;

	public SearchPanel(BookIndexing bi, APrintProperties props, IAPrintWait waitInterface, Object owner)
			throws Exception {
		this.bi = bi;
		this.props = props;
		this.waitInterface = waitInterface;
		this.owner = owner;
		initComponents();
	}

	private ISearchPanelListener searchPanelListener = null;

	private JComponent advancedsearchcomponent;

	public void setSearchPanelListener(ISearchPanelListener searchPanelListener) {
		this.searchPanelListener = searchPanelListener;
	}

	public ISearchPanelListener getSearchPanelListener() {
		return searchPanelListener;
	}

	private void initComponents() throws Exception {

		setLayout(new BorderLayout());

		JTabbedPane tabPane = new JTabbedPane();

		FormPanel spp = new FormPanel(getClass().getResourceAsStream("searchpropertiespanel.jfrm")); //$NON-NLS-1$

		JLabel labelsearchproperties = spp.getLabel("labelsearchproperties"); //$NON-NLS-1$
		assert labelsearchproperties != null;
		labelsearchproperties.setText(Messages.getString("SearchPanel.2")); //$NON-NLS-1$

		JLabel folderlabel = spp.getLabel("folderlabel"); //$NON-NLS-1$
		assert folderlabel != null;
		folderlabel.setText(Messages.getString("SearchPanel.4")); //$NON-NLS-1$

		foldervaluelabel = spp.getLabel("foldervaluelabel"); //$NON-NLS-1$
		foldervaluelabel.setToolTipText(Messages.getString("SearchPanel.6")); //$NON-NLS-1$
		assert foldervaluelabel != null;

		changeFolder = (JButton) spp.getButton("changeFolder"); //$NON-NLS-1$
		assert changeFolder != null;
		changeFolder.setToolTipText(Messages.getString("SearchPanel.8")); //$NON-NLS-1$
		changeFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeFolder();
			}
		});

		indexfolder = (JButton) spp.getButton("indexfolder"); //$NON-NLS-1$
		assert indexfolder != null;
		indexfolder.setToolTipText(Messages.getString("SearchPanel.10")); //$NON-NLS-1$
		indexfolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				indexFolder();
			}
		});
		indexfolder.setText(Messages.getString("SearchPanel.11")); //$NON-NLS-1$

		FormPanel sp = new FormPanel(getClass().getResourceAsStream("searchpanel.jfrm")); //$NON-NLS-1$

		JLabel searchlabel = sp.getLabel("searchlabel"); //$NON-NLS-1$
		searchlabel.setText(Messages.getString("SearchPanel.14")); //$NON-NLS-1$

		searchfield = sp.getTextField("searchfield"); //$NON-NLS-1$
		assert searchfield != null;

		KeyAdapter searchAdapterForTextField = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				search();
			}
		};
		searchfield.addKeyListener(searchAdapterForTextField);


		searchbutton = (JButton) sp.getButton("searchbutton"); //$NON-NLS-1$
		assert searchbutton != null;
		searchbutton.setText(Messages.getString("SearchPanel.17")); //$NON-NLS-1$
		searchbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				search();
			}
		});

		resultnumberlabel = (JLabel) sp.getLabel("resultnumberlabel"); //$NON-NLS-1$
		assert resultnumberlabel != null;

		JLabel namelabel = sp.getLabel("nameLabel"); //$NON-NLS-1$
		assert namelabel != null;
		namelabel.setText(Messages.getString("SearchPanel.20")); //$NON-NLS-1$

		nameFilterField = sp.getTextField("nameFilterField"); //$NON-NLS-1$
		assert nameFilterField != null;
		nameFilterField.addKeyListener(searchAdapterForTextField);

		JLabel instrumentlabel = sp.getLabel("instrumentlabel"); //$NON-NLS-1$
		assert instrumentlabel != null;
		instrumentlabel.setText(Messages.getString("SearchPanel.23")); //$NON-NLS-1$

		instrumentFilterField = sp.getTextField("instrumentFilterField"); //$NON-NLS-1$
		assert instrumentFilterField != null;
		instrumentFilterField.addKeyListener(searchAdapterForTextField);

		JLabel authorlabel = sp.getLabel("authorlabel"); //$NON-NLS-1$
		assert authorlabel != null;
		authorlabel.setText(Messages.getString("SearchPanel.26")); //$NON-NLS-1$

		authorFilterField = sp.getTextField("authorFilterField"); //$NON-NLS-1$
		assert authorFilterField != null;
		authorFilterField.addKeyListener(searchAdapterForTextField);

		JLabel genderlabel = sp.getLabel("genderLabel"); //$NON-NLS-1$
		assert genderlabel != null;
		genderlabel.setText(Messages.getString("SearchPanel.123")); //$NON-NLS-1$

		genderFilterField = sp.getTextField("gendertextfield"); //$NON-NLS-1$
		assert genderFilterField != null;
		genderFilterField.addKeyListener(searchAdapterForTextField);

		JLabel descriptionfilterlabel = sp.getLabel("descriptionfilterlabel"); //$NON-NLS-1$
		assert descriptionfilterlabel != null;
		descriptionfilterlabel.setText(Messages.getString("SearchPanel.29")); //$NON-NLS-1$

		descriptionFilterField = sp.getTextField("descriptionFilterField"); //$NON-NLS-1$
		assert descriptionFilterField != null;
		descriptionFilterField.addKeyListener(searchAdapterForTextField);

		JLabel arrangerlabel = sp.getLabel("arrangerlabel"); //$NON-NLS-1$
		assert arrangerlabel != null;
		arrangerlabel.setText(Messages.getString("SearchPanel.32")); //$NON-NLS-1$

		arrangerFilterField = sp.getTextField("arrangerFilterField"); //$NON-NLS-1$
		assert arrangerFilterField != null;
		arrangerFilterField.addKeyListener(searchAdapterForTextField);

		JLabel scaleFilterLabel = sp.getLabel("scaleFilterLabel"); //$NON-NLS-1$
		assert scaleFilterLabel != null;
		scaleFilterLabel.setText(Messages.getString("SearchPanel.35")); //$NON-NLS-1$

		scaleFilterField = sp.getTextField("scaleFilterField"); //$NON-NLS-1$
		assert scaleFilterField != null;
		scaleFilterField.addKeyListener(searchAdapterForTextField);

		chkboxExclusive = sp.getCheckBox("chkboxExclusive"); //$NON-NLS-1$
		assert chkboxExclusive != null;
		chkboxExclusive.setText(Messages.getString("SearchPanel.38")); //$NON-NLS-1$

		JLabel resultlabel = sp.getLabel("resultlabel"); //$NON-NLS-1$
		assert resultlabel != null;
		resultlabel.setText(Messages.getString("SearchPanel.42")); //$NON-NLS-1$

		advancedsearchcomponent = (JComponent) sp.getComponentByName("advancedsearchcomponent"); //$NON-NLS-1$
		assert advancedsearchcomponent != null;

		advancedsearchcomponent.setVisible(false);

		JButton toggleadvancedsearch = (JButton) sp.getButton("toggleadvancedsearch"); //$NON-NLS-1$
		assert toggleadvancedsearch != null;
		toggleadvancedsearch.setText(Messages.getString("SearchPanel.45")); //$NON-NLS-1$

		toggleadvancedsearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				advancedsearchcomponent.setVisible(!advancedsearchcomponent.isVisible());
				invalidate();

			}
		});

		searchcomponent = new JTable();
		JScrollPane tablescrollpane = new JScrollPane(searchcomponent);
		tablescrollpane.setAutoscrolls(true);

		sp.getFormAccessor().replaceBean("searchcomponent", tablescrollpane); //$NON-NLS-1$

		searchcomponent.setShowGrid(true);
		searchcomponent.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// searchcomponent.setAutoCreateRowSorter(true); // JDK 6

		ReadOnlyDefaultTableModel dm = new ReadOnlyDefaultTableModel();
		dm.addColumn("score"); //$NON-NLS-1$
		dm.addColumn("name"); //$NON-NLS-1$
		dm.addColumn("scale"); //$NON-NLS-1$
		dm.addColumn("instrument"); //$NON-NLS-1$
		dm.addColumn("genre"); //$NON-NLS-1$
		dm.addColumn("description"); //$NON-NLS-1$
		dm.addColumn("fileref"); //$NON-NLS-1$

		DefaultTableColumnModel tcm = new DefaultTableColumnModel();
		
		
		TableColumn tc = new TableColumn(0);
		
		tc.setHeaderValue("Score");
		tcm.addColumn(tc);
		
		tc = new TableColumn(1);
		tc.setHeaderValue(Messages.getString("SearchPanel.53")); //$NON-NLS-1$
		tc.setMinWidth(300);
		
		tcm.addColumn(tc);
		tc = new TableColumn(2);
		tc.setHeaderValue(Messages.getString("SearchPanel.54")); //$NON-NLS-1$
		tcm.addColumn(tc);

		tc = new TableColumn(3);
		tc.setHeaderValue(Messages.getString("SearchPanel.55")); //$NON-NLS-1$
		tcm.addColumn(tc);

		// instrument
		tc = new TableColumn(4);
		tc.setHeaderValue(Messages.getString("SearchPanel.56")); //$NON-NLS-1$
		tc.setMinWidth(300);
		tcm.addColumn(tc);

		tc = new TableColumn(5);
		tc.setHeaderValue(Messages.getString("SearchPanel.57")); //$NON-NLS-1$
		tcm.addColumn(tc);

		tc = new TableColumn(6);
		tc.setHeaderValue(Messages.getString("SearchPanel.58")); //$NON-NLS-1$
		tc.setMinWidth(1000);
		tcm.addColumn(tc);

		searchcomponent.setModel(dm);

		/*
		 * RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dm);
		 * searchcomponent.setRowSorter(sorter);
		 */
		searchcomponent.setColumnModel(tcm);

		searchcomponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clickOnTable(e);

			}
		});

		tabPane.addTab(Messages.getString("SearchPanel.124"), sp); //$NON-NLS-1$
		tabPane.addTab(Messages.getString("SearchPanel.125"), spp); //$NON-NLS-1$

		add(tabPane, BorderLayout.CENTER);

		checkUI();

	}

	private void clickOnTable(MouseEvent e) {
		if (searchPanelListener != null && e.getClickCount() >= 2)
			searchPanelListener.mouseDblClickOnElement();
	}

	private void changeFolder() {
		logger.debug("changeFolder"); //$NON-NLS-1$

		File sf = props.getSearchFolder();

		JDirectoryChooser c = new JDirectoryChooser(sf);
		if (c.showDialog(this, Messages.getString("SearchPanel.60")) == JDirectoryChooser.APPROVE_OPTION) { //$NON-NLS-1$
			File selectedFile = c.getSelectedFile();
			if (selectedFile != null) {
				logger.debug("selected file :" //$NON-NLS-1$
						+ selectedFile.getAbsolutePath());

				props.setSearchFolder(selectedFile);

				checkUI();

			}
		}

	}

	private void indexFolder() {
		try {
			final File sf = props.getSearchFolder();
			if (sf == null)
				return;
			logger.debug("indexFolder :" + sf); //$NON-NLS-1$

			Runnable r = new Runnable() {
				public void run() {
					try {

						waitInterface.infiniteStartWait(Messages.getString("SearchPanel.63")); //$NON-NLS-1$

						props.setSearchFolder(sf);

						bi.index(sf, new ProgressIndicator() {

							public void progress(double progress, String message) {
								waitInterface
										.infiniteChangeText(Messages.getString("SearchPanel.63") + " ... " + message);

							}
						});

						waitInterface.infiniteEndWait();

					} catch (Exception ex) {
						logger.error("error in indexing .." + ex.getMessage(), //$NON-NLS-1$
								ex);
						waitInterface.infiniteEndWait();
						JMessageBox.showMessage(owner, Messages.getString("SearchPanel.65") //$NON-NLS-1$
								+ ex.getMessage());
					}
				}
			};

			new Thread(r).start();

		} catch (Exception ex) {
			logger.error("error :" + ex.getMessage(), ex); //$NON-NLS-1$
			JMessageBox.showMessage(owner, Messages.getString("SearchPanel.67") //$NON-NLS-1$
					+ ex.getMessage());
		}
	}

	private void checkUI() {

		File s = props.getSearchFolder();
		String label = Messages.getString("SearchPanel.68"); //$NON-NLS-1$

		if (s == null) {
			indexfolder.setEnabled(false);
		} else {
			indexfolder.setEnabled(true);
			label = s.getAbsolutePath();
		}

		foldervaluelabel.setText(label);

	}

	private void search() {

		try {

			if (advancedsearchcomponent.isVisible()) {
				logger.debug("advanced search");//$NON-NLS-1$
				updateSearchComboFromAdvancedProperties();
				// then search
			}

			String searchText = searchfield.getText();

			logger.debug("search :" + searchText); //$NON-NLS-1$

			ScoredDocument[] search = bi.search(searchText);

			logger.debug("search done"); //$NON-NLS-1$

			DefaultTableModel model = (DefaultTableModel) searchcomponent.getModel();

			while (model.getRowCount() > 0)
				model.removeRow(0);

			for (int i = 0; i < search.length; i++) {
				ScoredDocument sdocument = search[i];
				double score = sdocument.score;
				Document document = sdocument.document;

				String name = document.get("name"); //$NON-NLS-1$
				String scale = document.get("scale"); //$NON-NLS-1$
				String instrument = document.get("instrument"); //$NON-NLS-1$
				String genre = document.get("genre"); //$NON-NLS-1$
				String description = document.get("description"); //$NON-NLS-1$
				String fileref = document.get("fileref"); //$NON-NLS-1$
				String all = document.get("all");

				model.addRow(new Object[] {"" + score, name, scale, genre, instrument, description, fileref });
			}

			DateFormatter df = new DateFormatter();
			df.setFormat(DateFormat.getTimeInstance());

			resultnumberlabel.setText("" + df.valueToString(new Date()) //$NON-NLS-1$
					+ " : (" + search.length + ")"); //$NON-NLS-1$ //$NON-NLS-2$

			model.fireTableDataChanged();

		} catch (Exception ex) {
			logger.error("error while searching :" + ex.getMessage(), ex); //$NON-NLS-1$
			
			resultnumberlabel.setText("Error in query :" + ex.getMessage());
			
			// JMessageBox.showMessage(owner, Messages.getString("SearchPanel.81") + ex.getMessage()); //$NON-NLS-1$
		}

	}

	private void addField(StringBuilder sb, String fieldname, boolean strict, JTextField field) {
		if (field == null)
			return;

		if (field.getText() != null && !"".equals(field.getText())) { //$NON-NLS-1$
			if (sb.length() > 0) {
				sb.append(" "); //$NON-NLS-1$
			}

			if (strict) {
				sb.append("+"); //$NON-NLS-1$
			}

			sb.append(fieldname).append(':').append("\"").append( //$NON-NLS-1$
					field.getText()).append("\""); //$NON-NLS-1$
		}
	}

	private void updateSearchComboFromAdvancedProperties() {
		try {

			StringBuilder sb = new StringBuilder();

			boolean strict = chkboxExclusive.isSelected();

			addField(sb, "name", strict, nameFilterField); //$NON-NLS-1$
			addField(sb, "scale", strict, scaleFilterField); //$NON-NLS-1$
			addField(sb, "instrument", strict, instrumentFilterField); //$NON-NLS-1$
			addField(sb, "author", strict, authorFilterField); //$NON-NLS-1$
			addField(sb, "description", strict, descriptionFilterField); //$NON-NLS-1$
			addField(sb, "arranger", strict, arrangerFilterField); //$NON-NLS-1$
			addField(sb, "genre", strict, genderFilterField); //$NON-NLS-1$

			searchfield.setText(sb.toString());

		} catch (Exception ex) {
			logger.error("error while searching :" + ex.getMessage(), ex); //$NON-NLS-1$
			JMessageBox.showMessage(owner, Messages.getString("SearchPanel.94") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	public URL[] getSelectedItems() throws Exception {

		int[] selectedRows = searchcomponent.getSelectedRows();
		DefaultTableModel tm = (DefaultTableModel) searchcomponent.getModel();

		ArrayList<URL> retvalue = new ArrayList<URL>();

		for (int i = 0; i < selectedRows.length; i++) {

			String fileref = (String) tm.getValueAt(selectedRows[i], 6);
			retvalue.add(new URL(fileref));
		}

		return retvalue.toArray(new URL[0]);
	}

	public void actionPerformed(ActionEvent e) {
		if ("SEARCH".equals(e.getActionCommand())) { //$NON-NLS-1$
			search();
		}
	}

	// test method ..

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout()));

		APrintProperties p = new APrintProperties(true);
		BookIndexing bi = new BookIndexing(p);

		IAPrintWait w = new IAPrintWait() {

			@Override
			public void infiniteStartWait(String text) {

			}

			@Override
			public void infiniteStartWait(String text, ICancelTracker cancelTracker) {
				// TODO Auto-generated method stub

			}

			@Override
			public void infiniteEndWait() {
				// TODO Auto-generated method stub

			}

			@Override
			public void infiniteChangeText(String text) {
				// TODO Auto-generated method stub

			}
		};
		SearchPanel searchPanel = new SearchPanel(bi, p, w, null);

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(searchPanel, BorderLayout.CENTER);

		f.setSize(800, 500);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

}
