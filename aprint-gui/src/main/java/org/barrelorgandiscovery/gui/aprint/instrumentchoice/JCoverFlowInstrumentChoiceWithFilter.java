package org.barrelorgandiscovery.gui.aprint.instrumentchoice;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNGImporterInternalFrame;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;

import com.jeta.forms.components.panel.FormPanel;

/**
 * component permitting to select the instrument
 * 
 * @author pfreydiere
 */
public class JCoverFlowInstrumentChoiceWithFilter extends JPanel {

	private static Logger logger = Logger.getLogger(JCoverFlowInstrumentChoiceWithFilter.class);

	JButton resetFilterButton;
	JButton searchButton;

	JTextField searchTextField;

	JCoverFlowInstrumentChoice instrumentChoice;

	/**
	 * constructor
	 * 
	 * @param repository2
	 * @param instrumentListener
	 */
	public JCoverFlowInstrumentChoiceWithFilter(Repository2 repository2, IInstrumentChoiceListener instrumentListener)
			throws Exception {

		InputStream fis = getClass().getResourceAsStream("coverflowwithfilter.jfrm");
		assert fis != null;
		FormPanel fp = new FormPanel(fis);

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

		JCoverFlowInstrumentChoice cfic = new JCoverFlowInstrumentChoice(repository2, instrumentListener);
		instrumentChoice = cfic;

		fp.getFormAccessor().replaceBean(fp.getComponentByName("instruments"), cfic);

		JLabel lblSearch = (JLabel) fp.getComponentByName("lblfilterinstruments");//$NON-NLS-1$
		lblSearch.setText(Messages.getString("APrint.161"));//$NON-NLS-1$

		searchTextField = (JTextField) fp.getComponentByName("txtfilter");//$NON-NLS-1$

		final ActionListener searchListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String filter = searchTextField.getText();
					logger.debug("search criteria :" + filter); //$NON-NLS-1$
					instrumentChoice.setInstrumentFilter(filter);
					instrumentChoice.reloadInstruments();
				} catch (Exception ex) {
					logger.error("error in defining the instrument filter : " //$NON-NLS-1$
							+ ex.getMessage(), ex);
				}
			}
		};

		searchTextField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchListener.actionPerformed(null);
				}
			}

		});

		resetFilterButton = (JButton) fp.getButton("reset"); //$NON-NLS-1$
		resetFilterButton.setText(Messages.getString("APrint.293")); //$NON-NLS-1$

		resetFilterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String filter = searchTextField.getText();
					logger.debug("old search criteria :" + filter); //$NON-NLS-1$
					instrumentChoice.setInstrumentFilter(null);
					instrumentChoice.reloadInstruments();
				} catch (Exception ex) {
					logger.error("error in resetting the instrument filter : " //$NON-NLS-1$
							+ ex.getMessage(), ex);
				}

			}
		});

		searchButton = (JButton) fp.getComponentByName("filterinstruments"); //$NON-NLS-1$
		searchButton.addActionListener(searchListener);
		searchButton.setText(Messages.getString("APrint.292")); //$NON-NLS-1$

	}

	public void selectInstrument(String instrumentName) {
		instrumentChoice.selectInstrument(instrumentName);
	}
	
	
}
