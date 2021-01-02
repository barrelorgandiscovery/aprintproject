package org.barrelorgandiscovery.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.barrelorgandiscovery.messages.Messages;

/**
 * Classe permettant l'affichage d'une boite de dialogue d'information pour
 * l'utilisateur
 * 
 * @author Freydiere Patrice
 */
public class JMessageBox extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 298443382958097434L;

	private JLabel label;

	private JButton button;

	public JMessageBox(Frame parent, String message) {
		super(parent);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		label = new JLabel(message);
		p.add(label, BorderLayout.CENTER);

		button = new JButton(Messages.getString("JMessageBox.0")); //$NON-NLS-1$
		button.setActionCommand(Messages.getString("JMessageBox.1")); //$NON-NLS-1$
		button.addActionListener(this);

		JPanel btnpanel = new JPanel();
		btnpanel.add(button);

		p.add(btnpanel, BorderLayout.SOUTH);

		getContentPane().add(p);
		pack();
	}

	// public static void showMessage(Frame parent, String message) {
	// JMessageBox mb = new JMessageBox(parent, message);
	// mb.setTitle(Messages.getString("JMessageBox.2")); //$NON-NLS-1$
	// mb.setModal(true);
	// mb.setLocationByPlatform(true);
	//
	// SwingUtils.center(mb);
	//
	// mb.setVisible(true);
	//
	// }

	/**
	 * Show a message
	 * 
	 * @param parent
	 *            the parent frame, or dialog, must be non NULL
	 * @param message
	 *            message to show to the user
	 */
	public static void showMessage(Object parent, String message) {
		if (parent instanceof Frame) {
			JOptionPane.showMessageDialog((Frame) parent, message);
		} else if (parent instanceof Dialog) {
			JOptionPane.showMessageDialog((Dialog) parent, message);
		} else if (parent instanceof Component) {
			JOptionPane.showMessageDialog((Component)parent, message);
		} else if (parent == null) {
			JOptionPane.showMessageDialog(null, message);
		} else {
			throw new RuntimeException("implementation error "
					+ parent.getClass().getName());
		}

	}

	/**
	 * display error message
	 * @param parent
	 * @param t
	 */
	public static void showError(Object parent, Throwable t) {
		String message = t.getMessage();
		if (message == null) {
			message = "";
		}
		showMessage(parent, "An error has been raised :" + message
				+ "\n" + "Please see logs");
	}

	public void actionPerformed(ActionEvent e) {
		if (Messages.getString("JMessageBox.3").equals(e.getActionCommand())) //$NON-NLS-1$
			setVisible(false);

	}

}
