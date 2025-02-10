package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.editableinstrument.EditableInstrument;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentStorage;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.editableinstrument.StreamStorageEditableInstrumentManager;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.importer.MidiBoekGammeImporter;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.VFSTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;

import com.birosoft.liquid.LiquidLookAndFeel;

/**
 * This is a component, including the view and the controller ...
 * Not directly used
 * 
 * 
 * @author Freydiere Patrice
 * 
 */
public class JInstrumentEditor extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -14054716700474819L;
	private static Logger logger = Logger.getLogger(JInstrumentEditor.class);

	private EditableInstrumentManager manager;

	private JInstrumentEditorPanel instrumentEditor;

	public JInstrumentEditor(EditableInstrumentManager manager) throws Exception {
		super();

		this.manager = manager;

		setTitle(Messages.getString("JInstrumentEditor.13")); //$NON-NLS-1$
		setIconImage(APrintNG.getAPrintApplicationIcon());

		initComponents();

	}

	/**
	 * init components
	 * @throws Exception
	 */
	private void initComponents() throws Exception {

		this.instrumentEditor = new JInstrumentEditorPanel(this);

		getContentPane().add(instrumentEditor, BorderLayout.CENTER);

		JMenuBar menuBar = new JMenuBar();

		createFileMenu(menuBar);

		createScaleMenu(menuBar);

		setJMenuBar(menuBar);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (!confirmAbandonModifications()) {

					return;
				}
				e.getWindow().dispose();
			}
		});

	}

	private void createScaleMenu(JMenuBar menuBar) {
		logger.debug("creating scale menu"); //$NON-NLS-1$
		JMenu scaleMenu = new JMenu(Messages.getString("JInstrumentEditor.77")); //$NON-NLS-1$
		menuBar.add(scaleMenu);

		JMenuItem importScaleFromFile = scaleMenu.add(Messages.getString("JInstrumentEditor.67")); //$NON-NLS-1$
		importScaleFromFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					chooseScale();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(JInstrumentEditor.this,
							Messages.getString("JInstrumentEditor.68") + ex.getMessage()); //$NON-NLS-1$
					BugReporter.sendBugReport();
				}
			}
		});

		JMenuItem importScaleFromMidiBoek = scaleMenu.add(Messages.getString("JInstrumentEditor.101")); //$NON-NLS-1$
		importScaleFromMidiBoek.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					importScaleFromMidiboek();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(JInstrumentEditor.this,
							"Fail to import midiboek scale :" + ex.getMessage()); //$NON-NLS-1$
					BugReporter.sendBugReport();

				}

			}
		});

	}

	private void createFileMenu(JMenuBar menuBar) {

		logger.debug("creating file menu"); //$NON-NLS-1$
		JMenu file = new JMenu(Messages.getString("JInstrumentEditor.25")); //$NON-NLS-1$
		menuBar.add(file);
		file.setMnemonic('f');

		JMenuItem newInstrument = file.add(Messages.getString("JInstrumentEditor.70")); //$NON-NLS-1$
		newInstrument.setAccelerator(KeyStroke.getKeyStroke("control N")); //$NON-NLS-1$
		newInstrument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					if (!confirmAbandonModifications())
						return;

					IEditableInstrument newInstrument = new EditableInstrument();
					instrumentEditor.setModel(newInstrument);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(JInstrumentEditor.this, Messages.getString("JInstrumentEditor.71") //$NON-NLS-1$
							+ ex.getMessage());
					BugReporter.sendBugReport();
				}
			}
		});

		file.addSeparator();

		JMenuItem openItem = file.add(Messages.getString("JInstrumentEditor.26")); //$NON-NLS-1$
		openItem.setAccelerator(KeyStroke.getKeyStroke("control O")); //$NON-NLS-1$
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					if (!confirmAbandonModifications())
						return;

					loadInstrument();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(JInstrumentEditor.this, Messages.getString("JInstrumentEditor.72") //$NON-NLS-1$
							+ ex.getMessage());
					BugReporter.sendBugReport();
				}
			}
		});

		JMenuItem saveItem = file.add(Messages.getString("JInstrumentEditor.29")); //$NON-NLS-1$
		saveItem.setAccelerator(KeyStroke.getKeyStroke("control S")); //$NON-NLS-1$
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					saveInstrument();
					JMessageBox.showMessage(JInstrumentEditor.this, Messages.getString("JInstrumentEditor.91")); //$NON-NLS-1$

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(JInstrumentEditor.this, Messages.getString("JInstrumentEditor.73") //$NON-NLS-1$
							+ ex.getMessage());
					BugReporter.sendBugReport();
				}
			}
		});

		JMenuItem deleteItem = file.add(Messages.getString("JInstrumentEditor.84")); //$NON-NLS-1$
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					deleteInstrument();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(JInstrumentEditor.this, Messages.getString("JInstrumentEditor.73") //$NON-NLS-1$
							+ ex.getMessage());
					BugReporter.sendBugReport();
				}
			}
		});

		file.addSeparator();

		JMenuItem importInstrument = file.add(Messages.getString("JInstrumentEditor.80")); //$NON-NLS-1$
		importInstrument.setAccelerator(KeyStroke.getKeyStroke("control I")); //$NON-NLS-1$
		importInstrument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					importInstrument();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(JInstrumentEditor.this, Messages.getString("JInstrumentEditor.81") //$NON-NLS-1$
							+ ex.getMessage());
					BugReporter.sendBugReport();
				}
			}

		});

		JMenuItem exportInstrument = file.add(Messages.getString("JInstrumentEditor.82")); //$NON-NLS-1$
		exportInstrument.setAccelerator(KeyStroke.getKeyStroke("control E")); //$NON-NLS-1$
		exportInstrument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					exportInstrument();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(JInstrumentEditor.this, Messages.getString("JInstrumentEditor.83") //$NON-NLS-1$
							+ ex.getMessage());
					BugReporter.sendBugReport();
				}

			}
		});

		file.addSeparator();
		JMenuItem close = file.add(Messages.getString("JInstrumentEditor.74")); //$NON-NLS-1$
		close.setAccelerator(KeyStroke.getKeyStroke("control Q")); //$NON-NLS-1$
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (!confirmAbandonModifications())
					return;

				setVisible(false);
				dispose();
			}
		});

	}

	private File lastOpenedFile = null;

	private void importScaleFromMidiboek() throws Exception {
		try {

			IEditableInstrument ei = instrumentEditor.getModel();

			assert ei != null;

			APrintFileChooser c = new APrintFileChooser();
			c.setMultiSelectionEnabled(false);
			c.setFileFilter(new VFSFileNameExtensionFilter("MidiBoek scale", "gam")); //$NON-NLS-1$ //$NON-NLS-2$
			if (c.showOpenDialog(this) == APrintFileChooser.APPROVE_OPTION) {
				// chargement de la gamme ...
				AbstractFileObject choosenFile = c.getSelectedFile();
				if (choosenFile != null) {

					FileName filename = choosenFile.getName();
					Scale s = MidiBoekGammeImporter.importScale(choosenFile.getInputStream(), filename.getBaseName());
					ei.setScale(s); // model fire events for the GUI ...
				}
			}

		} catch (Exception ex) {
			logger.error("importScaleFromMidiboek", ex); //$NON-NLS-1$
			// error message ...
			throw new Exception(ex.getMessage(), ex);
		}
	}

	private void importInstrument() throws Exception {
		APrintFileChooser fc = new APrintFileChooser();
		fc.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("JInstrumentEditor.27"), //$NON-NLS-1$
				new String[] { StreamStorageEditableInstrumentManager.EDITABLE_INSTRUMENT_TYPE })); // $NON-NLS-1$

		fc.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

		if (fc.showOpenDialog(JInstrumentEditor.this) == APrintFileChooser.APPROVE_OPTION) {

			final AbstractFileObject result = fc.getSelectedFile();

			InputStream zipis = result.getInputStream();
			EditableInstrumentStorage is = new EditableInstrumentStorage();
			FileName filename = result.getName();
			assert filename != null;
			IEditableInstrument newModel = is.load(zipis, filename.getBaseName());

			instrumentEditor.setModel(newModel);

			instrumentEditor.resetCurrentPipeStopGroup();
		}
	}

	private void loadInstrument() throws Exception {

		String[] instrumentList = manager.listEditableInstruments();
		String showInputDialog = (String) JOptionPane.showInputDialog(this, Messages.getString("JInstrumentEditor.3"), //$NON-NLS-1$
				Messages.getString("JInstrumentEditor.2"), //$NON-NLS-1$
				JOptionPane.QUESTION_MESSAGE, null, instrumentList,
				instrumentList.length > 0 ? instrumentList[0] : null);

		if (showInputDialog != null) {
			IEditableInstrument loadEditableInstrument = manager.loadEditableInstrument(showInputDialog);
			loadEditableInstrument.clearDirty();

			instrumentEditor.setModel(loadEditableInstrument);

			instrumentEditor.resetCurrentPipeStopGroup();
		}
	}

	/**
	 * Save instrument
	 * 
	 * @throws Exception
	 */
	private void saveInstrument() throws Exception {

		IEditableInstrument ei = instrumentEditor.getModel();
		assert ei != null;

		manager.saveEditableInstrument(ei);
		ei.clearDirty();
	}

	private void deleteInstrument() throws Exception {

		logger.debug("deleting instrument"); //$NON-NLS-1$
		String[] instrumentList = manager.listEditableInstruments();
		String showInputDialog = (String) JOptionPane.showInputDialog(this, Messages.getString("JInstrumentEditor.86"), //$NON-NLS-1$
				Messages.getString("JInstrumentEditor.87"), //$NON-NLS-1$
				JOptionPane.QUESTION_MESSAGE, null, instrumentList,
				instrumentList.length > 0 ? instrumentList[0] : null);

		if (showInputDialog != null) {
			manager.deleteEditableInstrument(showInputDialog);
		}
	}

	private void exportInstrument() throws FileNotFoundException, Exception, IOException {

		logger.debug("export instrument"); //$NON-NLS-1$

		APrintFileChooser fc = new APrintFileChooser();
		fc.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("JInstrumentEditor.30"), //$NON-NLS-1$
				StreamStorageEditableInstrumentManager.EDITABLE_INSTRUMENT_TYPE)); // $NON-NLS-1$

		int showDialog = fc.showDialog(JInstrumentEditor.this, Messages.getString("JInstrumentEditor.32")); //$NON-NLS-1$
		if (showDialog == APrintFileChooser.APPROVE_OPTION) {
			logger.debug("saving the instrument in a packed file ..."); //$NON-NLS-1$
			EditableInstrumentStorage is = new EditableInstrumentStorage();

			AbstractFileObject selectedFile = fc.getSelectedFile();
			FileName selectedFileName = selectedFile.getName();
			String filename = selectedFileName.getBaseName();
			if (!filename.endsWith(StreamStorageEditableInstrumentManager.EDITABLE_INSTRUMENT_TYPE)) { // $NON-NLS-1$
				logger.debug("append the extension"); //$NON-NLS-1$
				selectedFile = (AbstractFileObject) selectedFile.getFileSystem()
						.resolveFile(selectedFile.getName().toString() + "." //$NON-NLS-1$
								+ StreamStorageEditableInstrumentManager.EDITABLE_INSTRUMENT_TYPE);
			}

			OutputStream fileOutputStream = VFSTools.transactionalWrite(selectedFile);
			is.save(instrumentEditor.getModel(), fileOutputStream);

			fileOutputStream.close();

			logger.debug("instrument saved ..."); //$NON-NLS-1$

		}
	}

	/**
	 * return true if the user want to continue without keeping its modifications
	 * 
	 * @return
	 */
	private boolean confirmAbandonModifications() {
		IEditableInstrument currentModel = instrumentEditor.getModel();
		if (currentModel == null) {
			return true;
		}

		if (currentModel.isDirty()) {

			if (JOptionPane.showConfirmDialog(JInstrumentEditor.this, Messages.getString("JInstrumentEditor.99"), //$NON-NLS-1$
					Messages.getString("JInstrumentEditor.100"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) { //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}

	/**
	 * function called when choosing a scale ...
	 */
	private void chooseScale() throws Exception {
		try {

			IEditableInstrument ei = instrumentEditor.getModel();

			assert ei != null;

			APrintFileChooser c = new APrintFileChooser();
			c.setMultiSelectionEnabled(false);
			c.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("JInstrumentEditor.54"), "gamme")); //$NON-NLS-1$ //$NON-NLS-2$
			if (c.showOpenDialog(this) == APrintFileChooser.APPROVE_OPTION) {
				// chargement de la gamme ...
				AbstractFileObject choosenFile = c.getSelectedFile();
				if (choosenFile != null) {
					Scale s = ScaleIO.readGamme(choosenFile.getInputStream());
					ei.setScale(s); // model fire events for the GUI ...
				}
			}

		} catch (Exception ex) {
			logger.error("chooseScale", ex); //$NON-NLS-1$
			// error message ...
			throw new Exception(ex.getMessage(), ex);
		}
	}

	/**
	 * Test function ...
	 * 
	 * @param args
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure(new LF5Appender());

		UIManager.setLookAndFeel(new LiquidLookAndFeel());

		// model.setScale(ScaleIO.readGamme(new File("gammes/52li.gamme")));

		StreamStorageEditableInstrumentManager stm = new StreamStorageEditableInstrumentManager(new FolderStreamStorage(
				new File("/home/use/projets/APrint/reference_instruments"))); //$NON-NLS-1$

		JInstrumentEditor editor = new JInstrumentEditor(stm);
		editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		editor.setSize(800, 600);
		editor.setVisible(true);

	}

}
