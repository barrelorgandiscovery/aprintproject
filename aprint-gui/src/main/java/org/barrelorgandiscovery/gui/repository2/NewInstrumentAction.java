package org.barrelorgandiscovery.gui.repository2;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrument;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.gaerepositoryclient.GAESynchronizedRepository2;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.repository.JRepositoryInstrumentEditorPanel;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;

public class NewInstrumentAction extends RepositoryAbstractAction {

	private static Logger logger = Logger.getLogger(NewInstrumentAction.class);

	public NewInstrumentAction(Object parent,
			CurrentRepositoryInformations infos) {
		super(parent, infos);

	}

	@Override
	protected void safeActionPerformed(ActionEvent e) throws Exception {

		try {

			EditableInstrumentManagerRepository currentEditableInstrumentManagerRepository = infos
					.getCurrentEditableInstrumentManagerRepository();
			EditableInstrumentManager em = currentEditableInstrumentManagerRepository
					.getEditableInstrumentManager();

			EditableInstrument editableInstrument = new EditableInstrument();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// f.setAlwaysOnTop(true);
			f.setIconImage(APrintNG.getAPrintApplicationIcon());

			JRepositoryInstrumentEditorPanel p = new JRepositoryInstrumentEditorPanel(
					f);

			Container contentPane = f.getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(p, BorderLayout.CENTER);

			f.setSize(1024, 768);
			SwingUtils.center(f);
			p.edit(editableInstrument, null, em);

			f.setVisible(true);

		} catch (Throwable t) {
			logger.error("error in creating the new instrument ... "); //$NON-NLS-1$
			JMessageBox.showMessage(parent,
					Messages.getString("JRepositoryForm.17")); //$NON-NLS-1$
		}

	}

}
