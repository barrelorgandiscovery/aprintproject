package org.barrelorgandiscovery.gui.aprint;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionManager;
import org.barrelorgandiscovery.extensions.IExtensionName;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;


/**
 * GUI for the extension management in the application
 * 
 * @author Freydiere Patrice
 * 
 */
public class APrintExtensionList extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8659066636575275512L;

	private static Logger logger = Logger.getLogger(APrintExtensionList.class);

	private JList list;

	private ExtensionManager em;

	private APrint refAprint;

	/**
	 * Constructor
	 * 
	 * @param owner
	 *            the APrint application reference (for relaunching the
	 *            application after a change)
	 * @param em
	 *            the Extension Manager object
	 * @throws Exception
	 */
	public APrintExtensionList(APrint owner, ExtensionManager em)
			throws Exception {
		super(Messages.getString("APrintExtensionList.0")); //$NON-NLS-1$

		this.refAprint = owner;

		this.em = em;
		list = new JList(em.listJarExtensions());
		list.setBorder(new TitledBorder(Messages
				.getString("APrintExtensionList.1"))); //$NON-NLS-1$

		JPanel main = new JPanel();
		main.setLayout(new BorderLayout());

		main.add(list, BorderLayout.CENTER);

		JPanel buttons = new JPanel();

		JButton addNewException = new JButton(Messages
				.getString("APrintExtensionList.2")); //$NON-NLS-1$
		addNewException.setActionCommand("ADDBYURL"); //$NON-NLS-1$
		addNewException.addActionListener(this);

		JButton deleteException = new JButton(Messages
				.getString("APrintExtensionList.4")); //$NON-NLS-1$
		deleteException.setActionCommand("DELETE"); //$NON-NLS-1$
		deleteException.addActionListener(this);

		JButton updateExtension = new JButton(Messages
				.getString("APrintExtensionList.17")); //$NON-NLS-1$
		updateExtension.setActionCommand("UPDATE"); //$NON-NLS-1$
		updateExtension.addActionListener(this);

		buttons.add(addNewException);
		buttons.add(deleteException);
		buttons.add(updateExtension);

		main.add(buttons, BorderLayout.SOUTH);

		getContentPane().add(main);

		setIconImage(APrint.getAPrintApplicationIcon());

		setSize(800, 300);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if ("ADDBYURL".equals(cmd)) { //$NON-NLS-1$

			String url = JOptionPane.showInputDialog(Messages
					.getString("APrintExtensionList.7"), ""); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				em.downloadExtension(url);

				JMessageBox.showMessage(this, Messages
						.getString("APrintExtensionList.9")); //$NON-NLS-1$

				updateExtensionList();

				JMessageBox.showMessage(refAprint, Messages
						.getString("APrintExtensionList.21")); //$NON-NLS-1$
				refAprint.relaunch();

				setVisible(false);

			} catch (Exception ex) {
				logger.error("download", ex); //$NON-NLS-1$
				JMessageBox.showMessage(this, Messages
						.getString("APrintExtensionList.11") //$NON-NLS-1$
						+ ex.getMessage());
			}

		} else if ("DELETE".equals(cmd)) { //$NON-NLS-1$

			Object[] selectedValues = list.getSelectedValues();

			if (selectedValues == null || selectedValues.length == 0) {
				JMessageBox.showMessage(this, Messages
						.getString("APrintExtensionList.13")); //$NON-NLS-1$
			}

			try {

				for (int i = 0; i < selectedValues.length; i++) {
					Object object = selectedValues[i];
					em.invalidateExtension((IExtensionName) object);
				}

				updateExtensionList();

			} catch (Exception ex) {
				logger.error("delete", ex); //$NON-NLS-1$
				JMessageBox.showMessage(this, Messages
						.getString("APrintExtensionList.15")); //$NON-NLS-1$
			}

			JMessageBox.showMessage(this, Messages
					.getString("APrintExtensionList.16")); //$NON-NLS-1$

			refAprint.relaunch();

			setVisible(false);

		} else if ("UPDATE".equals(cmd)) { //$NON-NLS-1$

			Object[] selectedValues = list.getSelectedValues();

			if (selectedValues == null || selectedValues.length == 0) {
				JMessageBox.showMessage(this, Messages
						.getString("APrintExtensionList.13")); //$NON-NLS-1$
			}

			try {

				for (int i = 0; i < selectedValues.length; i++) {
					Object object = selectedValues[i];
					em.update((IExtensionName) object);
				}

				updateExtensionList();

				JMessageBox.showMessage(this, Messages
						.getString("APrintExtensionList.16")); //$NON-NLS-1$

				setVisible(false);

				// System.exit(0);

				refAprint.relaunch();

			} catch (Exception ex) {
				logger.error("update", ex); //$NON-NLS-1$
				JMessageBox.showMessage(this, Messages
						.getString("APrintExtensionList.20")); //$NON-NLS-1$
			}

		}

	}

	/**
	 * Update the extension list GUI
	 * 
	 * @throws Exception
	 */
	private void updateExtensionList() throws Exception {
		DefaultListModel lm = new DefaultListModel();
		IExtensionName[] el = em.listJarExtensions();
		for (int i = 0; i < el.length; i++) {
			IExtensionName extensionName = el[i];
			lm.addElement(extensionName);
		}

		list.setModel(lm);
	}
}
