package org.barrelorgandiscovery.gui.repository2;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrument;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.gaerepositoryclient.AbstractEditableInstrumentRepository;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.repository.JRepositoryInstrumentEditorPanel;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

public class ModifyInstrumentAction extends RepositoryAbstractAction {

	private static Logger logger = Logger.getLogger(ModifyInstrumentAction.class);

	public ModifyInstrumentAction(Object parent, CurrentRepositoryInformations infos) {
		super(parent, infos);
	}

	@Override
	protected void safeActionPerformed(ActionEvent e) throws Exception {
		try {

			Instrument currentInstrument = infos.getCurrentInstrument();
			if (currentInstrument == null)
				throw new Exception("no instrument selected"); //$NON-NLS-1$

			EditableInstrumentManagerRepository currentEditableInstrumentManagerRepository = infos
					.getCurrentInstrumentEditableInstrumentManagerRepository();

			if (currentEditableInstrumentManagerRepository == null)
				throw new Exception("cannot get editable instrumentmanager repository"); //$NON-NLS-1$

			EditableInstrumentManagerRepository em = currentEditableInstrumentManagerRepository;

			final String editableInstrumentName = em.findAssociatedEditableInstrumentName(currentInstrument.getName());
			final EditableInstrumentManager iem = em.getEditableInstrumentManager();

			if (editableInstrumentName == null)
				throw new Exception("unsupported repository for getting name"); //$NON-NLS-1$

			new Thread(new Runnable() {

				public void run() {
					try {
						infos.getWaitInterface().infiniteStartWait("load instrument"); //$NON-NLS-1$
						final IEditableInstrument editableInstrument = iem
								.loadEditableInstrument(editableInstrumentName);

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								try {
									JFrame f = new JFrame();
									f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
									// f.setAlwaysOnTop(true);
									f.setIconImage(APrintNG.getAPrintApplicationIcon());

									JRepositoryInstrumentEditorPanel p = new JRepositoryInstrumentEditorPanel(f);

									Container contentPane = f.getContentPane();
									contentPane.setLayout(new BorderLayout());
									contentPane.add(p, BorderLayout.CENTER);

									f.setSize(1024, 768);
									SwingUtils.center(f);
									p.edit(editableInstrument, null, iem);

									f.setVisible(true);
								} catch (Exception ex) {

									logger.error("error :" + ex.getMessage(), //$NON-NLS-1$
											ex);
									BugReporter.sendBugReport();
									JMessageBox.showError(parent, ex);

								} finally {
									infos.getWaitInterface().infiniteEndWait();
								}
							}
						});
					} catch (final Exception ex) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								infos.getWaitInterface().infiniteEndWait();
								logger.error("error :" + ex.getMessage(), ex); //$NON-NLS-1$
								BugReporter.sendBugReport();
								JMessageBox.showError(parent, ex);
							}
						});
					}
				}
			}).run();

		} catch (Throwable t) {
			logger.error("error in creating the new instrument ... ", t); //$NON-NLS-1$
			JMessageBox.showMessage(parent, Messages.getString("JRepositoryForm.17")); //$NON-NLS-1$
		}

	}

}
