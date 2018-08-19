package org.barrelorgandiscovery.gui.repository2;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;

public class DeleteAction extends RepositoryAbstractAction {

	private static Logger logger = Logger.getLogger(DeleteAction.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -8889592901860975246L;

	public DeleteAction(Object parent, CurrentRepositoryInformations infos) {
		super(parent, infos);
		
	}

	@Override
	protected void safeActionPerformed(ActionEvent e) throws Exception {

		Instrument ins = infos.getCurrentInstrument();

		if (ins == null)
			throw new Exception("no instrument");

		EditableInstrumentManagerRepository rep2 = infos
				.getCurrentInstrumentEditableInstrumentManagerRepository();

		if (rep2 == null)
			throw new Exception("not editable repository");

		if (JOptionPane.showConfirmDialog((Component) parent,
				Messages.getString("JRepositoryInstrumentEditorPanel.6")) == JOptionPane.YES_OPTION) { //$NON-NLS-1$

			logger.debug("delete instrument .. "); //$NON-NLS-1$

			String editableInstrumentName = rep2
					.findAssociatedEditableInstrumentName(ins.getName());

			if (editableInstrumentName != null)
				rep2.getEditableInstrumentManager().deleteEditableInstrument(
						editableInstrumentName);

		}
	}

}
