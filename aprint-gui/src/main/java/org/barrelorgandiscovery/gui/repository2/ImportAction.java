package org.barrelorgandiscovery.gui.repository2;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.SwingUtilities;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentConstants;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentStorage;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;

public class ImportAction extends RepositoryAbstractAction {

	public ImportAction(Object parent, CurrentRepositoryInformations infos) {
		super(parent, infos);
	}

	private static Logger logger = Logger.getLogger(ImportAction.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -5464748745302339760L;

	@Override
	protected void safeActionPerformed(ActionEvent e) throws Exception {

		EditableInstrumentManagerRepository eme = infos
				.getCurrentEditableInstrumentManagerRepository();

		EditableInstrumentManager im = eme.getEditableInstrumentManager();

		APrintProperties aprintproperties = infos.getAPrintProperties();

		APrintFileChooser fc = new APrintFileChooser();

		fc.setFileFilter(new VFSFileNameExtensionFilter("instrumentfile", //$NON-NLS-1$
				EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION));

		if (fc.showOpenDialog((Component) parent) == APrintFileChooser.APPROVE_OPTION) {
			AbstractFileObject f = fc.getSelectedFile();
			if (f != null) {
			
				logger.debug("loading instrument :"//$NON-NLS-1$
						+ f.toString());

				importInstrumentToRepository(im, f);

			}
		}

	}

	/**
	 * @param im
	 * @param f
	 * @throws FileNotFoundException
	 * @throws Exception
	 * @throws IOException
	 */
	protected void importInstrumentToRepository(
			final EditableInstrumentManager im, AbstractFileObject f) throws Exception {

		final EditableInstrumentStorage eis = new EditableInstrumentStorage();
		final InputStream fis = f.getInputStream();

		final IAPrintWait w = infos.getWaitInterface();

		w.infiniteStartWait(Messages.getString("APrintNG.203")); //$NON-NLS-1$

		new Thread(new Runnable() {
			public void run() {

				try {
					try {
					IEditableInstrument ei = eis
							.load(fis, "importedinstrument");//$NON-NLS-1$

					im.saveEditableInstrument(ei);

					logger.debug("instrument imported ...");//$NON-NLS-1$

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JMessageBox.showMessage(parent,
									Messages.getString("APrintNG.101")); //$NON-NLS-1$

						};

					});
					} finally {
						fis.close();
					}
				} catch (final Exception ex) {

					logger.error("error in exporting " + ex.getMessage(), ex);

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JMessageBox.showError(parent, ex);

						};
					});

				} finally {
					try {
						fis.close();
					} catch (Exception ex) {

					}
					w.infiniteEndWait();
				}

			}
		}).run();

	}
}
