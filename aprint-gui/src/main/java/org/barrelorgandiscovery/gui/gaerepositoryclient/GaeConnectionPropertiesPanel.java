package org.barrelorgandiscovery.gui.gaerepositoryclient;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gaerepositoryclient.APrintRepositoryClientConnection;
import org.barrelorgandiscovery.gaerepositoryclient.GAESynchronizedRepository2;
import org.barrelorgandiscovery.gaerepositoryclient.RepositoryInstrument;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.tools.SwingUtils;

import com.jeta.forms.components.panel.FormPanel;

public class GaeConnectionPropertiesPanel extends JPanel {

	private static Logger logger = Logger
			.getLogger(GaeConnectionPropertiesPanel.class);

	private APrintProperties properties;

	private JTextField urlfield;

	private JTextField login;

	private JTextField password;

	private JRadioButton anonymousAccess;

	private GAESynchronizedRepository2 rep;

	public GaeConnectionPropertiesPanel(APrintProperties props,
			GAESynchronizedRepository2 rep) throws Exception {

		assert rep != null;
		this.rep = rep;

		assert props != null;
		this.properties = props;

		FormPanel fpoptions = new FormPanel(getClass().getResourceAsStream(
				"connectionProperties.jfrm"));

		urlfield = fpoptions.getTextField("urltext");
		login = fpoptions.getTextField("login");
		password = fpoptions.getTextField("password");
		if (logger.isDebugEnabled()) {
			logger.debug("urlfield :" + urlfield);
			logger.debug("login :" + login);
			logger.debug("password :" + password);
		}

		String url = props.getWebRepositoryURL();
		if (url == null)
			url = "";
		urlfield.setText(url);

		KeyListener kl = new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				updateProperties();
			}
		};

		urlfield.addKeyListener(kl);
		password.addKeyListener(kl);
		urlfield.addKeyListener(kl);

		String l = props.getWebRepositoryUser();
		if (l == null)
			l = "";

		login.setText(l);

		String p = props.getWebRepositoryPassword();
		if (p == null)
			p = "";

		password.setText(p);

		final Component authPanel = fpoptions.getComponentByName("authpanel");

		anonymousAccess = (JRadioButton) fpoptions
				.getComponentByName("radioUsePublicConnexion");
		final JRadioButton radioUseAuthenticatedConnection = (JRadioButton) fpoptions
				.getComponentByName("radioUseAuthenticatedConnection");

		ButtonGroup g = new ButtonGroup();
		g.add(anonymousAccess);
		g.add(radioUseAuthenticatedConnection);

		boolean bauth = !"".equals(l);
		SwingUtils.recurseSetEnable((JComponent) authPanel, bauth);
		if (bauth) {
			radioUseAuthenticatedConnection.setSelected(true);
		} else {
			anonymousAccess.setSelected(true);
		}

		ActionListener cl = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (anonymousAccess.isSelected()) {
					SwingUtils.recurseSetEnable((JComponent) authPanel, false);
				} else {
					SwingUtils.recurseSetEnable((JComponent) authPanel, true);
				}
				updateProperties();
			}
		};

		anonymousAccess.addActionListener(cl);
		anonymousAccess.setActionCommand("anonymous");
		radioUseAuthenticatedConnection.addActionListener(cl);
		radioUseAuthenticatedConnection.setActionCommand("auth");

		JButton testConnection = (JButton) fpoptions
				.getButton("testConnection");
		testConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					logger.debug("test connection ... ");
					updateProperties();
					
					APrintRepositoryClientConnection r = new APrintRepositoryClientConnection();
					String cuser = properties.getWebRepositoryUser();

					String cpwd = properties.getWebRepositoryPassword();
					String curl = properties.getWebRepositoryURL();
					r.connect(curl, cuser, cpwd);

					RepositoryInstrument[] listInstruments = r
							.listInstruments();

					logger.debug("connection succeeded");

					GaeConnectionPropertiesPanel.this.rep
							.changeRepositoryConnectionInfos(new URL(curl),
									cuser, cpwd);
					
					JOptionPane.showMessageDialog(GaeConnectionPropertiesPanel.this, "Connection succeeded");

				} catch (Exception ex) {
					logger.error("error trying to connect to repository :"
							+ ex.getMessage(), ex);
					JOptionPane.showMessageDialog(
							GaeConnectionPropertiesPanel.this,
							"Error connecting the repository");
				}

			}
		});

		add(fpoptions);

	}

	protected void updateProperties() {

		String l = login.getText();
		String p = password.getText();
		String u = urlfield.getText();

		if ("".equals(u))
			u = null;

		if (anonymousAccess.isSelected()) {
			l = "";
			p = "";
		}

		properties.setWebRepositoryURL(u);
		properties.setWebRepositoryUser(l);
		properties.setWebRepositoryPassword(p);

	}


}
