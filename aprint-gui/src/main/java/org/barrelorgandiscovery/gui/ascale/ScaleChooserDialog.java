package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.SwingUtils;

public class ScaleChooserDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1046563960983587889L;

	public ScaleChooserDialog(Repository2 repository, Dialog owner,
			boolean modal) throws HeadlessException {
		super(owner, modal);
		initComponents(repository);
	}

	public ScaleChooserDialog(Repository2 repository, Frame owner, boolean modal)
			throws HeadlessException {
		super(owner, modal);
		initComponents(repository);
	}

	private ScaleChooserFromRepositoryComponent comp;

	public void initComponents(Repository2 repository) {
		this.comp = new ScaleChooserFromRepositoryComponent(repository);

		comp
				.addScaleChooserSelectionListener(new ScaleChooserSelectionListener() {

					public void selectionChanged(Scale selectedScale) {
						ScaleChooserDialog.this.selectedScale = selectedScale;
					}
				});

		getContentPane().add(comp, BorderLayout.CENTER);

		JButton ok = new JButton(Messages.getString("ScaleChooserDialog.0")); //$NON-NLS-1$
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				closed = false;
				setVisible(false);
			}
		});

		getContentPane().add(ok, BorderLayout.SOUTH);
		pack();
		SwingUtils.center(this);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (closed)
					selectedScale = null;
			}
		});

	}

	private boolean closed = true;

	private Scale selectedScale = null;

	public Scale getSelectedScale() {
		return selectedScale;
	}
	
	

}
