package org.barrelorgandiscovery.gui.repository2;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentConstants;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentStorage;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

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
		final EditableInstrumentManagerRepository eme = infos
				.getCurrentInstrumentEditableInstrumentManagerRepository();

		final Instrument currentInstrument = infos.getCurrentInstrument();
		if (currentInstrument == null)
			throw new Exception("no current Instrument"); //$NON-NLS-1$

		if (eme == null)
			throw new Exception("no current instrument repository"); //$NON-NLS-1$

		final EditableInstrumentManager im = eme.getEditableInstrumentManager();

		APrintProperties aprintproperties = infos.getAPrintProperties();

		JFileChooser fc = new JFileChooser(
				aprintproperties.getLastInstrumentBundleFileFolder());

		fc.setFileFilter(new FileNameExtensionFilter("instrumentfile", //$NON-NLS-1$
				EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION));

		if (fc.showSaveDialog((Component) parent) == JFileChooser.APPROVE_OPTION) {
			File fichier = fc.getSelectedFile();
			if (!fichier.getName().endsWith("." + EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION)) //$NON-NLS-1$
			{
				fichier = new File(fichier.getParentFile(), fichier.getName()
						+ "." + EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION); //$NON-NLS-1$
			}
			final File f = fichier;
			if (f != null) {
				aprintproperties.setLastInstrumentBundleFileFolder(f
						.getParentFile());

				logger.debug("export instrument :"//$NON-NLS-1$
						+ f.toString());

				final String instrumentName = eme
						.findAssociatedEditableInstrumentName(currentInstrument
								.getName());

				new Thread(new Runnable() {
					public void run() {
						infos.getWaitInterface().infiniteStartWait(
								Messages.getString("ExportAction.3")  + " " + currentInstrument.getName() //$NON-NLS-1$ //$NON-NLS-2$
										+ " " + Messages.getString("ExportAction.6") + " " + f.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						try {

							IEditableInstrument editableInstrument = im
									.loadEditableInstrument(instrumentName);

							EditableInstrumentStorage editableInstrumentStorage = new EditableInstrumentStorage();

							FileOutputStream fos = new FileOutputStream(f);
							try {

								editableInstrumentStorage.save(
										editableInstrument, fos);
							} finally {
								fos.close();
							}

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {

									JMessageBox.showMessage(parent,
											Messages.getString("ExportAction.8") + " " + f.getAbsolutePath() //$NON-NLS-1$ //$NON-NLS-2$
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
