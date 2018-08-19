package org.barrelorgandiscovery.gui.repository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.httpxmlrepository.HttpXmlRepository;
import org.barrelorgandiscovery.repository.httpxmlrepository.HttpXmlRepository.InstrumentDefinition;
import org.barrelorgandiscovery.tools.JMessageBox;

import com.jeta.forms.components.panel.FormPanel;

/**
 * present http xml repository properties
 * 
 * @author use
 * 
 */
public class JRepositoryHttpXMLPanel extends JAbstractRepositoryForm {

	private HttpXmlRepository r;
	private Object owner;
	private IAPrintWait wait = null;

	private static Logger logger = Logger
			.getLogger(JRepositoryHttpXMLPanel.class);

	public JRepositoryHttpXMLPanel(Object owner, Repository2 repository,
			APrintProperties properties) throws Exception {
		super(owner, repository, properties);

		assert repository instanceof HttpXmlRepository;

		this.r = (HttpXmlRepository) repository;
		this.owner = owner;

		initComponents();

	}

	public void setWait(IAPrintWait wait) {
		this.wait = wait;
	}

	/**
	 * init the components ...
	 * 
	 * @throws Exception
	 */
	private void initComponents() throws Exception {

		FormPanel fp = new FormPanel(getClass().getResourceAsStream(
				"httprepositoryproperties.jfrm"));
		setLayout(new BorderLayout());

		add(fp, BorderLayout.CENTER);

		JButton httpInformation = new JButton();
		httpInformation.setIcon(new ImageIcon(getClass().getResource(
				"network.png")));
		httpInformation.setText("Http Repository");

		fp.getFormAccessor().replaceBean("informations", httpInformation);

		JLabel lblurl = (JLabel) fp.getLabel("lblRepositoryurl");
		lblurl.setText("Repository URL :");

		JLabel tf = (JLabel) fp.getComponentByName("lblvalueurl");
		tf.setText(r.getHttpRootUrl());

		
		JLabel f = (JLabel)fp.getComponentByName("lblPresentAction");
		f.setText("HttpRepository associated actions");
		
		
		JButton btn = (JButton) fp.getButton("importInstruments");
		btn.setText("Import Instruments");
		btn.setIcon(new ImageIcon(getClass().getResource("remote.png")));
		btn.setToolTipText("Download all instrument from the webrepository");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				Thread t = new Thread() {
					@Override
					public void run() {

						final IAPrintWait w = wait; // transaction
						if (w != null) {
							w.infiniteStartWait("Downloading instruments on "
									+ r.getName());
						}
						try {
							logger.debug("importing instruments");
							try {
								InstrumentDefinition[] id = r.getInstruments();

								if (id != null && id.length > 0) {
									logger.debug("downloading instruments :"
											+ Arrays.asList(id));

									for (InstrumentDefinition d : id) {
										if (d == null) {
											continue;
										}

										if (w != null) {
											w.infiniteChangeText("Downloading "
													+ d.label);
										}
										r.downloadInstruments(new InstrumentDefinition[] { d });
									}

								}

							} catch (final Exception ex) {
								try {
									SwingUtilities
											.invokeAndWait(new Runnable() {

												@Override
												public void run() {
													JMessageBox.showError(
															owner, ex);
												}
											});
								} catch (Exception ex_) {
									logger.error("error :" + ex_.getMessage(),
											ex_);
								}

							}
						} finally {
							if (w != null) {
								w.infiniteEndWait();
							}
						}
						try {
							SwingUtilities.invokeAndWait(new Runnable() {

								@Override
								public void run() {
									JMessageBox.showMessage(owner,
											"Instruments downloaded");
								}
							});
						} catch (Exception ex_) {
							logger.error("error :" + ex_.getMessage(), ex_);
						}

					}
				};

				t.start();
			}
		});

	}

}
