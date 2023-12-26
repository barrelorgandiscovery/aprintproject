package org.barrelorgandiscovery.gui.repository;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentConstants;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentStorage;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.gui.ainstrument.JInstrumentEditorPanel;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.VFSTools;

/**
 * This class permit to modify an instrument
 * 
 * @author Freydiere Patrice
 * 
 */
public class JRepositoryInstrumentEditorPanel extends JPanel {

	private static final long serialVersionUID = -7612752846274572042L;

	private static Logger logger = Logger.getLogger(JRepositoryInstrumentEditorPanel.class);

	private JInstrumentEditorPanel editorPanel = null;

	private Frame owner;

	public JRepositoryInstrumentEditorPanel(Frame owner) throws Exception {
		this.owner = owner;

		initComponents();
	}

	JMenuBar menuBar;

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	private void initComponents() throws Exception {

		editorPanel = new JInstrumentEditorPanel(owner);
		setLayout(new BorderLayout());
		add(editorPanel, BorderLayout.CENTER);

		menuBar = new JMenuBar();

		JMenuItem save = new JMenuItem();
		ImageIcon saveImageIcon = new ImageIcon(
				ImageTools.loadImageAndCrop(getClass().getResourceAsStream("filesave.png"), 16, 16));//$NON-NLS-1$
		save.setIcon(saveImageIcon);
		
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		
		save.setToolTipText(Messages.getString("JRepositoryInstrumentEditorPanel.20")); //$NON-NLS-1$
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					saveCurrent();

				} catch (Exception ex) {
					logger.error("error in saving instrument .... " //$NON-NLS-1$
							+ ex.getMessage(), ex);
					JMessageBox.showMessage(owner, Messages.getString("JRepositoryInstrumentEditorPanel.3") //$NON-NLS-1$
							+ ex.getMessage());
				}
			}
		});

		menuBar.add(save);

		JMenuItem exportInstrument = new JMenuItem();

		ImageIcon exportImageIcon = new ImageIcon(
				ImageTools.loadImageAndCrop(getClass().getResourceAsStream("revert.png"), 16, 16));//$NON-NLS-1$
		exportInstrument.setIcon(exportImageIcon);

		exportInstrument.setToolTipText(Messages.getString("JRepositoryInstrumentEditorPanel.2")); //$NON-NLS-1$
		exportInstrument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {

					logger.debug("export instrument ..."); //$NON-NLS-1$

					APrintFileChooser fc = new APrintFileChooser();
					fc.setFileFilter(new VFSFileNameExtensionFilter("instrumentfile", //$NON-NLS-1$
							EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION));
					int showSaveDialog = fc.showSaveDialog(JRepositoryInstrumentEditorPanel.this);

					if (showSaveDialog == APrintFileChooser.APPROVE_OPTION) {

						AbstractFileObject choosenFile = fc.getSelectedFile();
						String filename = choosenFile.getName().getBaseName();
						if (!filename.endsWith("." //$NON-NLS-1$
								+ EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION)) {
							choosenFile = (AbstractFileObject) choosenFile.getFileSystem()
									.resolveFile(choosenFile.getName().toString() + "." //$NON-NLS-1$
											+ EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION);
						}

						logger.debug("save exported instrument :" //$NON-NLS-1$
								+ choosenFile.getName().toString());

						IEditableInstrument ei = editorPanel.getModel();
						EditableInstrumentStorage editableInstrumentStorage = new EditableInstrumentStorage();

						OutputStream fos = VFSTools.transactionalWrite(choosenFile);
						try {

							editableInstrumentStorage.save(ei, fos);
						} finally {
							fos.close();
						}

						logger.debug("file exported"); //$NON-NLS-1$
						JMessageBox.showMessage(owner, Messages.getString("JRepositoryInstrumentEditorPanel.9")); //$NON-NLS-1$
					}

				} catch (Exception ex) {
					logger.error("error in saving instrument :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
					JMessageBox.showMessage(owner, Messages.getString("JRepositoryInstrumentEditorPanel.11") //$NON-NLS-1$
							+ ex.getMessage());
				}

			}
		});

		menuBar.add(exportInstrument);

		JMenu scaleMenu = new JMenu("Scale ...");
		menuBar.add(scaleMenu);

		JMenuItem importScaleFromFile = new JMenuItem(Messages.getString("JRepositoryInstrumentEditorPanel.12")); //$NON-NLS-1$
		importScaleFromFile.setToolTipText(Messages.getString("JRepositoryInstrumentEditorPanel.13")); //$NON-NLS-1$
		importScaleFromFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					APrintFileChooser fc = new APrintFileChooser();
					fc.setFileFilter(
							new VFSFileNameExtensionFilter(Messages.getString("JRepositoryInstrumentEditorPanel.14"), //$NON-NLS-1$
									ScaleIO.SCALE_FILE_EXTENSION));

					if (fc.showOpenDialog(JRepositoryInstrumentEditorPanel.this) != APrintFileChooser.APPROVE_OPTION)
						return;

					AbstractFileObject selected = fc.getSelectedFile();
					if (selected == null)
						return;

					InputStream istream = selected.getInputStream();
					try {
						Scale readGamme = ScaleIO.readGamme(istream);
						IEditableInstrument m = editorPanel.getModel();
						m.setScale(readGamme);

						editorPanel.setModel(m);
					} finally {
						istream.close();
					}

				} catch (Exception ex) {
					logger.error("error in loading scale :" + ex.getMessage(), //$NON-NLS-1$
							ex);
					JMessageBox.showMessage(owner, Messages.getString("JRepositoryInstrumentEditorPanel.16") //$NON-NLS-1$
							+ ex.getMessage());
				}
			}
		});

		scaleMenu.add(importScaleFromFile);

		JMenuItem exportScaleToFile = new JMenuItem(Messages.getString("JRepositoryInstrumentEditorPanel.30")); //$NON-NLS-1$
		exportScaleToFile.setToolTipText(Messages.getString("JRepositoryInstrumentEditorPanel.31")); //$NON-NLS-1$
		exportScaleToFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					APrintFileChooser fc = new APrintFileChooser();
					fc.setFileFilter(
							new VFSFileNameExtensionFilter(Messages.getString("JRepositoryInstrumentEditorPanel.32"), //$NON-NLS-1$
									ScaleIO.SCALE_FILE_EXTENSION));

					if (fc.showSaveDialog(JRepositoryInstrumentEditorPanel.this) != APrintFileChooser.APPROVE_OPTION)
						return;

					AbstractFileObject selected = fc.getSelectedFile();
					if (selected == null)
						return;

					String filename = selected.getName().getBaseName();
					if (!filename.endsWith(ScaleIO.SCALE_FILE_EXTENSION))
						selected = (AbstractFileObject) selected.getFileSystem()
								.resolveFile(selected.getName().toString() + "." + ScaleIO.SCALE_FILE_EXTENSION);

					IEditableInstrument m = editorPanel.getModel();
					Scale instrumentScale = m.getScale();

					OutputStream ostream = VFSTools.transactionalWrite(selected);
					try {
						ScaleIO.writeGamme(instrumentScale, ostream);
					} finally {
						ostream.close();
					}
					JMessageBox.showMessage(owner, Messages.getString("JRepositoryInstrumentEditorPanel.33") //$NON-NLS-1$
							+ selected.getName() + Messages.getString("JRepositoryInstrumentEditorPanel.34")); //$NON-NLS-1$

				} catch (Exception ex) {
					logger.error("error in save scale :" + ex.getMessage(), //$NON-NLS-1$
							ex);
					JMessageBox.showMessage(owner, Messages.getString("JRepositoryInstrumentEditorPanel.35") //$NON-NLS-1$
							+ ex.getMessage());
				}
			}
		});

		scaleMenu.add(exportScaleToFile);

	}

	private EditableInstrumentManager instrumentManager = null;

	private String editableInstrumentName;

	/**
	 * Activate the edition of an instrument
	 * 
	 * @param instrument
	 * @param insmanager
	 */
	public void edit(IEditableInstrument instrument, String editableInstrumentName,
			EditableInstrumentManager insmanager) {

		this.editableInstrumentName = editableInstrumentName;

		this.instrumentManager = insmanager;

		editorPanel.setModel(instrument);

	}

	private void saveCurrent() throws Exception {
		logger.debug("saving instrument .. "); //$NON-NLS-1$
		instrumentManager.saveEditableInstrument(editorPanel.getModel());
		JMessageBox.showMessage(owner, Messages.getString("JRepositoryInstrumentEditorPanel.10")); //$NON-NLS-1$
	}

	private void deleteCurrent() throws Exception {
		logger.debug("delete instrument .. "); //$NON-NLS-1$

		if (editableInstrumentName != null)
			instrumentManager.deleteEditableInstrument(editableInstrumentName);

		remove(editorPanel);
		invalidate();
		validate();
		repaint();
	}

}
