package org.barrelorgandiscovery.messages;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.APrint;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.jeta.forms.components.panel.FormPanel;


/**
 * This class is a frame that help people to translate the software in several
 * languages
 * 
 * @author Freydiere Patrice
 * 
 */
public class JTranslator extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6632824656356911121L;

	private static Logger logger = Logger.getLogger(JTranslator.class);

	private File propertyfile;

	private Properties editedProperties;
	private int currentKeyIndex;
	private LinkedList<String> keyCollection;

	private ResourceBundle bundle;

	private APrintProperties aprintproperties;

	public JTranslator(File propertyfile, ResourceBundle bundle,
			APrintProperties aprintproperties) throws Exception {

		this.aprintproperties = aprintproperties;

		setTitle(Messages.getString("JTranslator.1")); //$NON-NLS-1$
		setIconImage(APrint.getAPrintApplicationIcon());

		// build content need aprintproperties

		buildContent();

		assert propertyfile != null;
		assert bundle != null;
		assert aprintproperties != null;

		this.propertyfile = propertyfile;
		this.bundle = bundle;

		Properties props = new Properties();
		if (propertyfile.exists()) {

			logger.debug("file exist , loading the properties ... "); //$NON-NLS-1$
			FileInputStream fis = new FileInputStream(propertyfile);
			try {
				props.load(new BufferedInputStream(fis));
			} finally {
				fis.close();
			}
		}

		this.editedProperties = props;

		Enumeration<String> englishkeys = bundle.getKeys();

		TreeSet<String> forSorting = new TreeSet<String>();

		while (englishkeys.hasMoreElements()) {

			String k = englishkeys.nextElement();
			logger.debug("key :" + k); //$NON-NLS-1$
			forSorting.add(k);
		}

		keyCollection = new LinkedList<String>(forSorting);

		logger.debug("all keys loaded ..."); //$NON-NLS-1$
		if (keyCollection.size() > 0)
			setCurrentKey(0);

	}

	private JLabel labelkey;
	private JButton previous;
	private JButton next;
	private JButton searchButton;
	private JTextField searchText;

	private JButton closeAndSave;
	private JButton deletefile;

	private JTextArea englishMessage;
	private JTextArea localizedMessage;

	private JCheckBox displaykeys;

	private boolean changeMatter = true;

	private void buildContent() throws Exception {

		FormPanel fp = new FormPanel(getClass().getResourceAsStream(
				"translationform.jfrm")); //$NON-NLS-1$

		labelkey = fp.getLabel("key"); //$NON-NLS-1$
		previous = (JButton) fp.getButton("previous"); //$NON-NLS-1$
		previous.setToolTipText(Messages.getString("JTranslator.6")); //$NON-NLS-1$
		previous.setIcon(new ImageIcon(getClass().getResource("1leftarrow.png"))); //$NON-NLS-1$
		previous.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentKeyIndex > 0)
					setCurrentKey(currentKeyIndex - 1);
			}
		});

		next = (JButton) fp.getButton("next"); //$NON-NLS-1$
		next.setToolTipText(Messages.getString("JTranslator.8")); //$NON-NLS-1$
		next.setIcon(new ImageIcon(getClass().getResource("1rightarrow.png"))); //$NON-NLS-1$
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentKeyIndex < keyCollection.size() - 1)
					setCurrentKey(currentKeyIndex + 1);
			}
		});

		searchText = fp.getTextField("search"); //$NON-NLS-1$

		searchButton = (JButton) fp.getButton("searchbutton"); //$NON-NLS-1$
		searchButton.setText(Messages.getString("JTranslator.10")); //$NON-NLS-1$
		searchButton.setToolTipText(Messages.getString("JTranslator.11")); //$NON-NLS-1$
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchLookup(searchText.getText());
			}
		});

		englishMessage = (JTextArea) fp.getComponentByName("englishmessage"); //$NON-NLS-1$
		localizedMessage = (JTextArea) fp
				.getComponentByName("localizedmessage"); //$NON-NLS-1$

		localizedMessage.getDocument().addDocumentListener(
				new DocumentListener() {

					public void changedUpdate(DocumentEvent e) {
						changeText();
					}

					public void insertUpdate(DocumentEvent e) {
						changeText();
					}

					public void removeUpdate(DocumentEvent e) {
						changeText();
					}

					private void changeText() {

						if (!changeMatter)
							return;

						String key = keyCollection.get(currentKeyIndex);
						editedProperties.setProperty(key, localizedMessage
								.getText());
					}
				});

		closeAndSave = (JButton) fp.getButton("save"); //$NON-NLS-1$
		closeAndSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					saveProperties();

					JOptionPane.showMessageDialog(JTranslator.this, Messages
							.getString("JTranslator.5")); //$NON-NLS-1$

					JTranslator.this.setVisible(false);

				} catch (Exception ex) {
					logger.error("saving properties :" + ex.getMessage(), ex); //$NON-NLS-1$
				}
			}
		});

		displaykeys = fp.getCheckBox("displaykeys"); //$NON-NLS-1$
		displaykeys.setText(Messages.getString("JTranslator.17")); //$NON-NLS-1$
		displaykeys.setToolTipText(Messages.getString("JTranslator.18")); //$NON-NLS-1$
		displaykeys.setSelected(aprintproperties
				.getDisplayKeyNamesForTranslation());
		displaykeys.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				aprintproperties.setDisplayKeyNamesForTranslation(displaykeys
						.isSelected());
			}

		});

		JLabel labelenglishtext = fp.getLabel("labelenglishtext"); //$NON-NLS-1$
		labelenglishtext.setText(Messages.getString("JTranslator.20") + "(" + Messages.getString("JTranslator.34") + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		JLabel labeltotranslate = fp.getLabel("labellocaltext"); //$NON-NLS-1$
		labeltotranslate.setText(Messages.getString("JTranslator.22") + "(" + Messages.getString("JTranslator.0") + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		deletefile = (JButton) fp.getButton("deletefile"); //$NON-NLS-1$
		deletefile.setText(Messages.getString("JTranslator.24")); //$NON-NLS-1$
		deletefile.setToolTipText(Messages.getString("JTranslator.25")); //$NON-NLS-1$

		deletefile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					if (propertyfile.delete()) {
						JOptionPane
								.showMessageDialog(
										JTranslator.this,
										Messages.getString("JTranslator.26") + propertyfile.getAbsolutePath() + " " //$NON-NLS-1$ //$NON-NLS-2$
												+ Messages
														.getString("JTranslator.28")); //$NON-NLS-1$
					} else {
						JOptionPane.showMessageDialog(JTranslator.this,
								Messages.getString("JTranslator.29") //$NON-NLS-1$
										+ propertyfile.getAbsolutePath());
					}

				} catch (Exception ex) {
					logger.error("failure in delete local file :" //$NON-NLS-1$
							+ ex.getMessage());
					BugReporter.sendBugReport();

					JOptionPane.showMessageDialog(JTranslator.this, Messages
							.getString("JTranslator.31") //$NON-NLS-1$
							+ ex.getMessage());
				}
			}
		});

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(fp, BorderLayout.CENTER);

		setSize(600, 400);

	}

	/**
	 * Set the current key and update the panel
	 * 
	 * @param key
	 */
	private void setCurrentKey(int i) {
		if (!(i >= 0 && i < keyCollection.size()))
			return;

		logger.debug("setting Current Key " + i); //$NON-NLS-1$

		this.currentKeyIndex = i;
		String key = keyCollection.get(i);

		this.labelkey.setText(Messages.getString("JTranslator.33") + key); //$NON-NLS-1$

		englishMessage.setText(bundle.getString(key));

		String overrideMessage = editedProperties.getProperty(key);
		if (overrideMessage == null)
			overrideMessage = Messages.getStringWithoutKey(key);

		changeMatter = false;
		localizedMessage.setText(overrideMessage);
		changeMatter = true;

	}

	private void saveProperties() throws Exception {

		// check the existance of the file ....
		if (!propertyfile.exists()) {
			logger.debug("create the directory"); //$NON-NLS-1$
			File parentDirectory = propertyfile.getParentFile();
			if (!parentDirectory.exists()) {
				boolean created = parentDirectory.mkdirs();
				if (!created)
					throw new Exception("fail to create " //$NON-NLS-1$
							+ parentDirectory.getAbsolutePath());
			}
		}

		FileOutputStream fileOutputStream = new FileOutputStream(propertyfile);
		try {
			editedProperties.store(fileOutputStream, "edited " //$NON-NLS-1$
					+ DateFormat.getDateInstance().format(new Date()) + " by " //$NON-NLS-1$
					+ System.getProperty("user")); //$NON-NLS-1$
		} finally {
			fileOutputStream.close();
		}
	}

	private void searchLookup(String textToSearch) {

		if (textToSearch == null || "".equals(textToSearch)) //$NON-NLS-1$
			return;

		int searchKey = currentKeyIndex + 1;

		while (searchKey < keyCollection.size()) {

			String key = keyCollection.get(searchKey);
			if (key.indexOf(textToSearch) != -1) {
				setCurrentKey(searchKey);
				return;
			}

			// search for default messages ...
			String englishone = bundle.getString(key);

			if (englishone.indexOf(textToSearch) != -1) {
				setCurrentKey(searchKey);
				return;
			}

			String overrideMessage = editedProperties.getProperty(key);
			if (overrideMessage == null)
				overrideMessage = Messages.getStringWithoutKey(key);

			if (overrideMessage.indexOf(textToSearch) != -1) {
				setCurrentKey(searchKey);
				return;
			}

			searchKey++;
		}

		int result = JOptionPane.showConfirmDialog(null, Messages
				.getString("JTranslator.2")); //$NON-NLS-1$
		if (result == JOptionPane.YES_OPTION) {
			currentKeyIndex = -1;
			searchLookup(textToSearch);
		}

	}
	// public static void main(String[] args) throws Exception {
	//
	// BasicConfigurator.configure(new LF5Appender());
	// Messages.initLocale(new File(System.getProperty("user.home")
	// + "/aprint-beta"));
	// JFrame f = new JFrame();
	// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//
	// f.getContentPane().setLayout(new BorderLayout());
	// f.getContentPane().add(
	// new JTranslation(Messages.getOverrideLocalizedMessageFile(),
	// Messages.getEnglishBundle()), BorderLayout.CENTER);
	//
	// f.pack();
	// f.setVisible(true);
	// }

}
