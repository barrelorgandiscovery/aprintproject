package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

/**
 * Browse Instrument by images
 * 
 * @author use
 * 
 */
public class JInstrumentTileViewer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3732150759797647883L;

	private static Logger logger = Logger.getLogger(JInstrumentTileViewer.class);

	private Repository2 rep;
	private JList l;

	private InstrumentSelectedListener instrumentSelectedListener;

	public void setInstrumentSelectedListener(InstrumentSelectedListener instrumentSelectedListener) {
		this.instrumentSelectedListener = instrumentSelectedListener;
	}

	public InstrumentSelectedListener getInstrumentSelectedListener() {
		return instrumentSelectedListener;
	}

	private static class InsVerticalRenderer implements ListCellRenderer {

		private JLabel labelImage = new JLabel();
		private JLabel labelText = new JLabel();

		public JPanel p = new JPanel();

		public InsVerticalRenderer() {

			BorderLayout bl = new BorderLayout();
			bl.setHgap(3);
			p.setLayout(bl);
			labelImage.setAlignmentY(BOTTOM_ALIGNMENT);
			p.add(labelImage, BorderLayout.CENTER);
			labelImage.setHorizontalAlignment(SwingConstants.CENTER);
			p.add(labelText, BorderLayout.SOUTH);
			labelText.setHorizontalAlignment(SwingConstants.CENTER);
			// small space in the bottom
			p.setBorder(new EmptyBorder(2, 2, 10, 2));

		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			try {
				Instrument ins = (Instrument) value;

				labelImage.setIcon(new ImageIcon(ins.getMiniPicture()));
				labelImage.setMaximumSize(new Dimension(200, 200));

				labelText.setText(ins.getName());

				p.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground")
						: UIManager.getColor("Table.background"));

				return p;

			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				return p;
			}
		}
	}

	public JInstrumentTileViewer(Repository2 rep) {

		initComponents();
		changeRepository(rep);
	}

	protected void initComponents() {

		l = new JList();

		l.setLayoutOrientation(JList.VERTICAL_WRAP);
		l.setCellRenderer(new InsVerticalRenderer());
		l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		l.setSelectionModel(new DefaultListSelectionModel());

		l.addListSelectionListener(iscl);
		l.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					Instrument i = (Instrument) l.getSelectedValue();
					if (i != null) {
						selectionChanged(i);
						if (instrumentSelectedListener != null) {
							try {

								instrumentSelectedListener.instrumentDoubleClicked(i);

							} catch (Throwable ex) {
								logger.error("error in instrument selection :" + ex.getMessage(), ex);
								JMessageBox.showError(null, ex);
								BugReporter.sendBugReport();
							}
						}
					}
				}
			}
		});

		setLayout(new BorderLayout());

		JScrollPane sp = new JScrollPane(l);

		add(sp, BorderLayout.CENTER);

		l.setBorder(new EmptyBorder(3, 3, 3, 3));

	}

	private class InternalSelectionChangeListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {

			JList l = (JList) e.getSource();

			if (l != null) {
				Instrument v = (Instrument) l.getSelectedValue();
				selectionChanged(v);
			}

		}
	}

	private Instrument currentSelectedInstrument;

	public Instrument getSelectedInstrument() {
		return currentSelectedInstrument;
	}

	protected void selectionChanged(Instrument ins) {
		if (logger.isDebugEnabled())
			logger.debug(" instrument changed :" + ins);

		this.currentSelectedInstrument = ins;

		if (instrumentSelectedListener != null) {
			try {

				instrumentSelectedListener.instrumentSelected(ins);

			} catch (Throwable ex) {
				logger.error("error in instrument selection :" + ex.getMessage(), ex);
				JMessageBox.showError(null, ex);
				BugReporter.sendBugReport();
			}
		}

	}

	private InternalSelectionChangeListener iscl = new InternalSelectionChangeListener();

	private class InternalRepositoryChangeListener implements RepositoryChangedListener {

		public void transformationAndImporterChanged() {

		}

		public void scalesChanged() {

		}

		public void instrumentsChanged() {
			reloadInstruments();
		}
	}

	private InternalRepositoryChangeListener icr = new InternalRepositoryChangeListener();

	public void changeRepository(Repository2 rep) {

		if (this.rep != null)
			this.rep.removeRepositoryChangedListener(icr);

		this.rep = rep;
		this.rep.addRepositoryChangedListener(icr);

		reloadInstruments();

	}

	private String filterName = null;

	public void setNameFilter(String filterName) {
		this.filterName = filterName;
		reloadInstruments();
	}

	public Repository2 getCurrentRepository() {
		return this.rep;
	}

	/**
	 * 
	 */
	protected void reloadInstruments() {
		Vector<Instrument> vinstrument = loadInstrument();

		DefaultListModel dlm = new DefaultListModel();
		for (Instrument ins : vinstrument) {
			if (filterName == null || ins.getName().toLowerCase().indexOf(filterName.toLowerCase()) != -1) {
				dlm.addElement(ins);
			}
		}

		l.setModel(dlm);
	}

	/**
	 * 
	 * @return
	 */
	protected Vector<Instrument> loadInstrument() {

		Instrument[] instruments = rep.listInstruments();

		Vector<Instrument> vinstrument = new Vector<Instrument>();
		for (int i = 0; i < instruments.length; i++) {
			Instrument ins = instruments[i];
			vinstrument.add(ins);
		}

		return vinstrument;
	}

	@Override
	public void doLayout() {

		int instrumentNumber = l.getModel().getSize();
		if (instrumentNumber > 0) {

			Component c = l.getCellRenderer().getListCellRendererComponent(l, l.getModel().getElementAt(0), 0, false,
					false);

			Dimension preferredSizeOfOneTile = c.getPreferredSize();
			if (preferredSizeOfOneTile.height > 0) {
				// System.out.println("preferred height :" +
				// preferredSize.height);
				int newrowCount = getHeight() / preferredSizeOfOneTile.height;
				// System.out.println("new row count :" + newrowCount);
				l.setVisibleRowCount(newrowCount);
			}
		}

		super.doLayout();

	}

	/**
	 * Tests unitaire du composant
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		final JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Properties p = new Properties();
		p.setProperty("repositorytype", "folder");
		p.setProperty("folder", "C:/Users/use/aprintstudio/private");

		APrintProperties props = new APrintProperties(true);
		Repository2 rep = Repository2Factory.create(p, props);

		JInstrumentTileViewer insp = new JInstrumentTileViewer(rep);
		insp.setPreferredSize(new Dimension(300, 300));
		// insp.setAutoscrolls(true);
		// sc.setViewportView(insp);

		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(insp, BorderLayout.CENTER);

		f.setSize(500, 500);
		f.setVisible(true);
		f.addPropertyChangeListener("size", new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				f.validate();

			}
		});

	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		this.l.addMouseListener(l);
	}

	@Override
	public synchronized void removeMouseListener(MouseListener l) {
		this.l.removeMouseListener(l);
	}

}
