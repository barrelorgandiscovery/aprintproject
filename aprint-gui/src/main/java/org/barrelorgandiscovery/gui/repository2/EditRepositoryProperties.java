package org.barrelorgandiscovery.gui.repository2;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import org.barrelorgandiscovery.gui.repository.JAbstractRepositoryForm;
import org.barrelorgandiscovery.gui.repository.RepositoryGUIFormFactory;
import org.barrelorgandiscovery.tools.SwingUtils;

public class EditRepositoryProperties extends RepositoryAbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8692123739130478715L;

	public EditRepositoryProperties(Object parent,
			CurrentRepositoryInformations infos) {
		super(parent, infos);
	}

	/*
	 * (non-Javadoc)
	 * @see org.barrelorgandiscovery.gui.repository2.RepositoryAbstractAction#safeActionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	protected void safeActionPerformed(ActionEvent e) throws Exception {

		JDialog d = new JDialog();

		RepositoryGUIFormFactory f = new RepositoryGUIFormFactory();
		JAbstractRepositoryForm rform = f.createAssociatedForm(
				infos.getOwner(), infos.getCurrentRepository2(),
				infos.getAPrintProperties());

		d.getContentPane().setLayout(new BorderLayout());
		d.getContentPane().add(rform, BorderLayout.CENTER);

		d.setSize(800, 600);
		SwingUtils.center(d);
		d.setTitle("Repository Properties :" + infos.getCurrentRepository2().getName());
		d.setModal(true);
		d.setVisible(true);

	}
}
