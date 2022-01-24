package org.barrelorgandiscovery.gui.repository2;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.OutputStream;

import javax.swing.SwingUtilities;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentConstants;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentStorage;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.ui.tools.VFSTools;

public class ExportAction extends RepositoryAbstractAction {

	private static Logger logger = Logger.getLogger(ExportAction.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -882122191870876841L;

	public ExportAction(Object parent, CurrentRepositoryInformations infos) {
		super(parent, infos);
	}

	@Override
	protected void safeActionPerformed(ActionEvent e) throws Exception {
		final EditableInstrumentManagerRepository eme = infos.getCurrentInstrumentEditableInstrumentManagerRepository();

		final Instrument currentInstrument = infos.getCurrentInstrument();
		if (currentInstrument == null)
			throw new Exception("no current Instrument"); //$NON-NLS-1$

		if (eme == null)
			throw new Exception("no current instrument repository"); //$NON-NLS-1$

		final EditableInstrumentManager im = eme.getEditableInstrumentManager();

		APrintProperties aprintproperties = infos.getAPrintProperties();

		APrintFileChooser fc = new APrintFileChooser();

		fc.setFileFilter(new VFSFileNameExtensionFilter("instrumentfile", //$NON-NLS-1$
				EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION));

		if (fc.showSaveDialog((Component) parent) == APrintFileChooser.APPROVE_OPTION) {
			AbstractFileObject fichier = fc.getSelectedFile();
			String filename = fichier.getName().getBaseName();
			if (!filename.endsWith("." + EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION)) //$NON-NLS-1$
			{
				fichier = (AbstractFileObject) fichier.getFileSystem().resolveFile(
						fichier.getName().toString() + "." + EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION); //$NON-NLS-1$
			}
			final AbstractFileObject f = fichier;
			if (f != null) {

				logger.debug("export instrument :"//$NON-NLS-1$
						+ f.toString());

				final String instrumentName = eme.findAssociatedEditableInstrumentName(currentInstrument.getName());

				new Thread(new Runnable() {
					public void run() {
						infos.getWaitInterface().infiniteStartWait(
								Messages.getString("ExportAction.3") + " " + currentInstrument.getName() //$NON-NLS-1$ //$NON-NLS-2$
										+ " " + Messages.getString("ExportAction.6") + " " + f.getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						try {

							IEditableInstrument editableInstrument = im.loadEditableInstrument(instrumentName);

							EditableInstrumentStorage editableInstrumentStorage = new EditableInstrumentStorage();

							OutputStream fos = VFSTools.transactionalWrite(f);
							try {

								editableInstrumentStorage.save(editableInstrument, fos);
							} finally {
								fos.close();
							}

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {

									JMessageBox.showMessage(parent,
											Messages.getString("ExportAction.8") + " " + f.getName() //$NON-NLS-1$ //$NON-NLS-2$
													+ " " + Messages.getString("ExportAction.11")); //$NON-NLS-1$ //$NON-NLS-2$

								};
							});

						} catch (final Exception ex) {
							logger.error(Messages.getString("ExportAction.12") + ex.getMessage(), ex); //$NON-NLS-1$
							BugReporter.sendBugReport();

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {

									JMessageBox.showError(parent, ex);

								};
							});
						} finally {
							infos.getWaitInterface().infiniteEndWait();
						}
					}
				}).run();

			}
		}

	}
}
