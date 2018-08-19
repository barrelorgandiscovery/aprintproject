package org.barrelorgandiscovery.gui.gaerepositoryclient;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;

import org.barrelorgandiscovery.messages.Messages;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.components.separator.TitledSeparator;


public class GaeRepositoryClientConnection extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6917529899908566869L;

	public GaeRepositoryClientConnection(Frame ownerFrame, boolean modal)
			throws Exception {
		super(ownerFrame, modal);
		initComponents();

	}

	private FormPanel thepanel;
	private JTextField textWebRepositoryUrl;
	private JTextField loginTextField;
	private JTextField passwordTextField;

	protected void initComponents() throws Exception {

		InputStream is = getClass().getResourceAsStream("gaeparameters.jfrm"); //$NON-NLS-1$
		if (is == null)
			throw new Exception("form not found"); //$NON-NLS-1$
		thepanel = new FormPanel(is);

		((TitledSeparator)thepanel.getComponentByName("labelParameters")).setText( //$NON-NLS-1$
				Messages.getString("GaeRepositoryClientConnection.1")); //$NON-NLS-1$

		thepanel.getLabel("labelwebrepositoryurl").setText( //$NON-NLS-1$
				Messages.getString("GaeRepositoryClientConnection.3")); //$NON-NLS-1$
		thepanel.getLabel("labelLogin").setText(Messages.getString("GaeRepositoryClientConnection.5")); //$NON-NLS-1$ //$NON-NLS-2$
		thepanel.getLabel("labelPassword").setText(Messages.getString("GaeRepositoryClientConnection.7")); //$NON-NLS-1$ //$NON-NLS-2$

		JButton buttonTest = (JButton) thepanel.getButton("buttonTest"); //$NON-NLS-1$
		buttonTest.setText(Messages.getString("GaeRepositoryClientConnection.9")); //$NON-NLS-1$
		buttonTest.setToolTipText(Messages.getString("GaeRepositoryClientConnection.10")); //$NON-NLS-1$

		JButton buttonOK = (JButton) thepanel.getButton("okButton"); //$NON-NLS-1$
		buttonOK.setText(Messages.getString("GaeRepositoryClientConnection.12")); //$NON-NLS-1$
		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel = false;
				setVisible(false);
			}
		});

		JButton buttonCancel = (JButton) thepanel.getButton("cancelButton"); //$NON-NLS-1$
		buttonCancel.setText(Messages.getString("GaeRepositoryClientConnection.14")); //$NON-NLS-1$
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel = true;
				setVisible(false);
			}
		});

		textWebRepositoryUrl = thepanel.getTextField("textWebRepositoryUrl"); //$NON-NLS-1$
		loginTextField = thepanel.getTextField("loginTextField"); //$NON-NLS-1$
		passwordTextField = thepanel.getTextField("passwordTextField"); //$NON-NLS-1$

		setLayout(new BorderLayout());
		getContentPane().add(thepanel, BorderLayout.CENTER);
	}

	private boolean cancel = true;

	public boolean isCanceled() {
		return cancel;
	}

	public String getLogin() {

		String t = loginTextField.getText();
		if ("".equals(t)) { //$NON-NLS-1$
			return null;
		}
		return t;

	}

	public String getPassword() {
		String t = passwordTextField.getText();
		if ("".equals(t)) { //$NON-NLS-1$
			return null;
		}
		return t;
	}

	public String getWebUrl() {
		String t = textWebRepositoryUrl.getText();
		if ("".equals(t)) { //$NON-NLS-1$
			return null;
		}
		return t;
	}

	public void setLogin(String login) {
		if (login == null)
			loginTextField.setText(""); //$NON-NLS-1$
		else
			loginTextField.setText(login);
	}

	public void setPassword(String password) {
		if (password == null)
			passwordTextField.setText(""); //$NON-NLS-1$
		else
			passwordTextField.setText(password);
	}

	public void setWebUrl(String weburl) {
		textWebRepositoryUrl.setText(weburl);
	}

}
