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

public class DuplicateInstrumentAction extends RepositoryAbstractAction {

	private static Logger logger = Logger
			.getLogger(DuplicateInstrumentAction.class);

	public DuplicateInstrumentAction(Object parent,
			CurrentRepositoryInformations infos) {
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
				throw new Exception(
						"cannot get editable instrumentmanager repository"); //$NON-NLS-1$

			EditableInstrumentManagerRepository em = currentEditableInstrumentManagerRepository;

			final String editableInstrumentName = em
					.findAssociatedEditableInstrumentName(currentInstrument
							.getName());
			final EditableInstrumentManager instrumentem = em
					.getEditableInstrumentManager();

			if (editableInstrumentName == null)
				throw new Exception("unsupported repository for getting name"); //$NON-NLS-1$

			String showInputDialog = JOptionPane.showInputDialog(null, "new instrument name", "duplicate instrument", JOptionPane.INFORMATION_MESSAGE);
			if (showInputDialog == null || showInputDialog.length() == 0) {
				return;
			}
			
			final IEditableInstrument editableInstrument = instrumentem
					.loadEditableInstrument(editableInstrumentName);			
			editableInstrument.setName(showInputDialog);
			
			
			instrumentem.saveEditableInstrument(editableInstrument);
			
		} catch (Throwable t) {
			logger.error("error in creating the new instrument ... ", t); //$NON-NLS-1$
			JMessageBox.showMessage(parent,
					Messages.getString("JRepositoryForm.17")); //$NON-NLS-1$
		}

	}

}
