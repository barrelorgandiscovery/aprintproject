package org.barrelorgandiscovery.gui.aprint.markers;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.GlobalVirtualBookUndoOperation;
import org.barrelorgandiscovery.gui.aedit.IVirtualBookChangedListener;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.AbstractEvent;
import org.barrelorgandiscovery.virtualbook.MarkerEvent;
import org.barrelorgandiscovery.virtualbook.Fragment;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import com.jeta.forms.components.panel.FormPanel;

/**
 * Panel for managing the markers
 * 
 * @author pfreydiere
 * 
 */
public class JMarkerComponents extends JPanel {

	/**
	 * serial
	 */
	private static final long serialVersionUID = -4866349281020828281L;

	/**
	 * logger
	 */
	private static Logger logger = Logger.getLogger(JMarkerComponents.class);

	/**
	 * the current virtualbook
	 */
	private JEditableVirtualBookComponent ec;

	/**
	 * Component for managing the List of Markers
	 */
	private JList list;

	/**
	 * Listeners for the marker and virtual book changes
	 */
	private Vector<IMarkerChangedListener> listeners = new Vector<IMarkerChangedListener>();

	/**
	 * Class to display elements in the list
	 * 
	 * @author use
	 * 
	 */
	private class MarkerDisplayer {

		/**
		 * the marker event reference
		 */
		private MarkerEvent event;

		/**
		 * Constructor
		 * 
		 * @param event
		 *            passed by reference
		 */
		public MarkerDisplayer(MarkerEvent event) {
			assert event != null;
			this.event = event;
		}

		@Override
		public String toString() {
			return event.getMarkerName();
		}
	}

	// data flavor for the markers
	private static DataFlavor markerDataFlavor = new DataFlavor(
			MarkerDisplayer.class, "Marker"); //$NON-NLS-1$

	/**
	 * Class for specifying the drag and drop content
	 * 
	 * @author pfreydiere
	 * 
	 */
	class MarkerTransferHandler extends TransferHandler {

		private int[] indices = null;

		// private int addIndex = -1; // Location where items were added
		// private int addCount = 0; // Number of items added.

		public boolean canImport(TransferHandler.TransferSupport info) {
			// Check for String flavor
			if (!info.isDataFlavorSupported(markerDataFlavor)) {
				logger.debug("nok, dataflavor is not supported in canImport"); //$NON-NLS-1$
				return false;
			}
			// logger.debug("ok can import");
			return true;
		}

		protected Transferable createTransferable(final JComponent c) {

			return new Transferable() {

				public boolean isDataFlavorSupported(DataFlavor flavor) {
					return flavor.equals(markerDataFlavor);
				}

				public DataFlavor[] getTransferDataFlavors() {
					return new DataFlavor[] { markerDataFlavor };
				}

				public Object getTransferData(DataFlavor flavor)
						throws UnsupportedFlavorException, IOException {
					JList l = (JList) c;
					Object[] values = l.getSelectedValues();
					indices = l.getSelectedIndices();
					logger.debug("indices for the drag :" //$NON-NLS-1$
							+ Arrays.toString(indices));
					return values;
				}
			};

		}

		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY_OR_MOVE;
		}

		public boolean importData(TransferHandler.TransferSupport info) {
			if (!info.isDrop()) {
				logger.debug("not a drop"); //$NON-NLS-1$
				return false;
			}

			logger.debug("ok let's go"); //$NON-NLS-1$

			JList list = (JList) info.getComponent();
			DefaultListModel listModel = (DefaultListModel) list.getModel();
			JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
			int index = dl.getIndex();
			boolean insert = dl.isInsert();

			// Get the string that is being dropped.
			Transferable t = info.getTransferable();
			Object[] data;
			try {
				data = (Object[]) t.getTransferData(markerDataFlavor);
			} catch (Exception e) {
				logger.error("error in getting data " + e.getMessage(), e); //$NON-NLS-1$
				return false;
			}

			// Perform the actual import.
			logger.debug("add the selected markers at index " + index); //$NON-NLS-1$
			for (int i = data.length - 1; i >= 0; i--) {

				MarkerDisplayer md = (MarkerDisplayer) data[i];
				logger.debug("adding marker :" + md); //$NON-NLS-1$
				if (indices != null) {
					// adjust the indices
					logger.debug("adjust indices"); //$NON-NLS-1$
					for (int j = 0; j < indices.length; j++) {
						if (indices[j] >= index) {
							indices[j]++;
						}

					}
				}

				MarkerEvent event = md.event;

				VirtualBook virtualBook = ec.getVirtualBook();

				ec.getUndoStack().push(
						new GlobalVirtualBookUndoOperation(ec.getVirtualBook(),
								Messages.getString("JMarkerComponents.9"), ec)); //$NON-NLS-1$

				Fragment markerSelection = virtualBook.selectMarker(event);
				logger.debug("marker selection :" + markerSelection); //$NON-NLS-1$

				List<MarkerEvent> markers = virtualBook.listMarkers();

				if (logger.isDebugEnabled()) {
					dumpMarkers("all Markers before insert :"); //$NON-NLS-1$
				}

				long l = 0; // by default at the beginning
				// getting the beginning of the insert place
				if (index >= 0 && index < markers.size()) {
					MarkerEvent m = markers.get(index);
					logger.debug("insert before the beginning of marker " + m); //$NON-NLS-1$
					l = m.getTimestamp();
				} else if (index == markers.size()) {
					logger.debug("insert the marker at the end of the book"); //$NON-NLS-1$
					l = virtualBook.getLength(); // insert at end
				}
				logger.debug("insert marker " + event + " at " + l); //$NON-NLS-1$ //$NON-NLS-2$
				virtualBook.insertAt(markerSelection, l);

				if (logger.isDebugEnabled()) {
					dumpMarkers("all Markers after insert :"); //$NON-NLS-1$
				}

			}

			return true;
		}

		protected void exportDone(JComponent c, Transferable data, int action) {
			cleanup(c, action == TransferHandler.MOVE);
			refreshList();
			fireMarkerChangedListener();
		}

		protected void cleanup(JComponent c, boolean remove) {
			logger.debug("clean up"); //$NON-NLS-1$

			if (remove && indices != null) {

				VirtualBook virtualBook = ec.getVirtualBook();
				List<MarkerEvent> markers = virtualBook.listMarkers();
				logger.debug("Current Marker List :" + markers); //$NON-NLS-1$
				for (int i = indices.length - 1; i >= 0; i--) {
					logger.debug("indice for the marker :" + indices[i]); //$NON-NLS-1$
					MarkerEvent marker = markers.get(indices[i]);
					logger.debug("removing Marker" + marker); //$NON-NLS-1$
					Fragment selectMarker = virtualBook.selectMarker(marker);
					virtualBook.removeFragment(selectMarker);
				}
			}
			indices = null;

		}
	}

	public JMarkerComponents() throws Exception {
		initComponents();
	}

	private void dumpMarkers(String libelle) {
		ArrayList<String> s = new ArrayList<String>();
		List<MarkerEvent> markers = ec.getVirtualBook().listMarkers();
		for (Iterator iterator = markers.iterator(); iterator.hasNext();) {
			MarkerEvent markerEvent = (MarkerEvent) iterator.next();
			s.add("" + markerEvent + " (len:" //$NON-NLS-1$ //$NON-NLS-2$
					+ ec.getVirtualBook().getMarkerLength(markerEvent) + ")"); //$NON-NLS-1$
		}

		logger.debug(libelle + ":" + s); //$NON-NLS-1$

	}

	private JButton delete;

	private void initComponents() throws Exception {

		String formName = "formMarkers.jfrm"; //$NON-NLS-1$

		InputStream is = getClass().getResourceAsStream(formName);
		if (is == null)
			throw new Exception("form " + formName + " not found"); //$NON-NLS-1$ //$NON-NLS-2$

		FormPanel fp = new FormPanel(is);

		// get key components
		list = (JList) fp.getComponentByName("listmarkers"); //$NON-NLS-1$
		if (list == null)
			throw new Exception("list cannot be found"); //$NON-NLS-1$

		list.setSelectionModel(new DefaultListSelectionModel());

		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				logger.debug("selection changed for list"); //$NON-NLS-1$
				JList l = (JList) e.getSource();
				checkDeleteState();
			}

		});

		delete = (JButton) fp.getComponentByName("btndelete"); //$NON-NLS-1$
		delete.setText(Messages.getString("JMarkerComponents.100")); //$NON-NLS-1$
		delete.setToolTipText(Messages.getString("JMarkerComponents.101")); //$NON-NLS-1$
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					VirtualBook virtualBook = ec.getVirtualBook();
					if (virtualBook == null)
						return;

					ec.getUndoStack().push(
							new GlobalVirtualBookUndoOperation(virtualBook,
									Messages.getString("JMarkerComponents.32"), ec)); //$NON-NLS-1$

					Object[] selectedObjects = list.getSelectedValues();
					for (int i = 0; i < selectedObjects.length; i++) {
						MarkerDisplayer md = (MarkerDisplayer) selectedObjects[i];
						Fragment sel = virtualBook.selectMarker(md.event);
						virtualBook.removeFragment(sel);
					}

					refreshList();
					checkDeleteState();
					ec.repaint();

				} catch (Exception ex) {
					logger.error(
							"error in deleting markers :" + ex.getMessage(), ex); //$NON-NLS-1$
					BugReporter.sendBugReport();
				}

			}
		});

		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				logger.debug("click on item"); //$NON-NLS-1$

			}

		});

		list.setDragEnabled(true);
		list.setTransferHandler(new MarkerTransferHandler());
		list.setDropMode(DropMode.INSERT);

		checkDeleteState();

		JTextArea textArea = (JTextArea) fp.getComponentByName("infos"); //$NON-NLS-1$
		textArea.setText(Messages.getString("JMarkerComponents.36") + "\n" //$NON-NLS-1$
				+ Messages.getString("JMarkerComponents.37") + "\n" //$NON-NLS-1$
				+ Messages.getString("JMarkerComponents.38")); //$NON-NLS-1$
		textArea.setEnabled(false);

		JLabel lblTitle = (JLabel) fp.getComponentByName("lblmarkers"); //$NON-NLS-1$
		lblTitle.setText(Messages.getString("JMarkerComponents.40")); //$NON-NLS-1$

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

	}

	/**
	 * @param l
	 */
	private void checkDeleteState() {
		Object[] values = list.getSelectedValues();
		if (values == null || values.length == 0) {
			delete.setEnabled(false);
		} else {
			delete.setEnabled(true);
		}
	}

	/**
	 * Define the virtualBook we are working on
	 * 
	 * @param vb
	 */
	public void setEditableComponent(JEditableVirtualBookComponent comp) {
		this.ec = comp;

		refreshList();
		this.ec.addVirtualBookChangedListener(new IVirtualBookChangedListener() {

			public void virtualBookChanged(VirtualBook newVirtualBook) {
				refreshList();
			}
		});

	}

	/**
	 * Update JList content from the VirtualBook
	 */
	protected void refreshList() {

		logger.debug("refresh Marker List"); //$NON-NLS-1$

		DefaultListModel model = (DefaultListModel) list.getModel();
		if (model == null)
			return;

		model.removeAllElements();
		VirtualBook virtualBook = ec.getVirtualBook();
		if (virtualBook == null)
			return;

		logger.debug("create new model"); //$NON-NLS-1$

		DefaultListModel dlm = new DefaultListModel();

		ArrayList<AbstractEvent> markerEventsList = virtualBook.findEvents(
				-1000000,
				virtualBook.getFirstHoleStart() + virtualBook.getLength(),
				MarkerEvent.class);

		for (Iterator iterator = markerEventsList.iterator(); iterator
				.hasNext();) {
			AbstractEvent ite = (AbstractEvent) iterator.next();
			assert ite instanceof MarkerEvent;
			MarkerEvent markerEvent = (MarkerEvent) ite;

			MarkerDisplayer md = new MarkerDisplayer(markerEvent);
			logger.debug("adding marker to the display list:" + md); //$NON-NLS-1$
			dlm.addElement(md);

		}

		list.setModel(dlm);

	}

	// public static void main(String[] args) throws Exception {
	// SwingUtilities.invokeAndWait(new Runnable() {
	// public void run() {
	// try {
	// launch();
	// } catch (Exception ex) {
	// ex.printStackTrace(System.err);
	// }
	// }
	// });
	//
	// }

	// private static void launch() throws Exception {
	//
	// BasicConfigurator.configure(new LF5Appender());
	//
	// JFrame f = new JFrame();
	// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//
	// VirtualBookResult vb = VirtualBookXmlIO
	// .read(new File(
	// "C:\\Users\\use\\Dropbox\\APrint\\Books\\27-29\\American_Patrol.book"));
	//
	// JMarkerComponents m = new JMarkerComponents();
	//
	// VirtualBook v = vb.virtualBook;
	// v.addEvent(new MarkerEvent(0, "Debut"));
	// v.addEvent(new MarkerEvent(100000, "Refrain"));
	//
	// f.getContentPane().setLayout(new BorderLayout());
	// f.getContentPane().add(m, BorderLayout.CENTER);
	//
	// m.setVirtualBook(v);
	//
	// f.setSize(500, 500);
	// f.setVisible(true);
	//
	// }

	public void addMarkerChangedListener(IMarkerChangedListener listener) {
		if (listener == null) {
			logger.warn("null listener passed"); //$NON-NLS-1$
			return;
		}
		listeners.add(listener);
	}

	public void removeMarkerChangedListener(IMarkerChangedListener listener) {
		listeners.remove(listener);
	}

	protected void fireMarkerChangedListener() {
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
			IMarkerChangedListener l = (IMarkerChangedListener) iterator.next();
			try {
				l.markersChanged();
			} catch (Exception ex) {
				logger.error("error firing marker change :" + ex.getMessage(), //$NON-NLS-1$
						ex);
			}
		}
	}

}
