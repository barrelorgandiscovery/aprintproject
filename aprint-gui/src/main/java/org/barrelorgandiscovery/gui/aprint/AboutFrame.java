package org.barrelorgandiscovery.gui.aprint;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextPane;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.barrelorgandiscovery.messages.Messages;

import com.jeta.forms.components.panel.FormPanel;


/**
 * Fenetre d'information sur le programme et les dernière évolutions
 * 
 * @author Freydiere Patrice
 * 
 */
public class AboutFrame extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 784468354848495612L;

	private JTextPane textpanel = new JTextPane();

	public AboutFrame(Frame owner, String title, boolean modal) throws Exception {
		super(owner, title, modal);
		
		FormPanel p = new FormPanel(getClass().getResourceAsStream("aboutform.jfrm"));
		
		textpanel = (JTextPane)p.getComponentByName("textArea");
	
		textpanel.setEditable(false);

		JButton ok = (JButton)p.getComponentByName("closeButton");
		ok.setText(Messages.getString("AboutFrame.0")); //$NON-NLS-1$
		
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});


		getContentPane().add(p);

		setSize(500,600);
		

	}

	public void setAboutContent(InputStream xmlinputStream) throws Exception {

		String language = "en"; //$NON-NLS-1$
		if ("fr".equals(Locale.getDefault().getLanguage())) //$NON-NLS-1$
			language = "fr"; //$NON-NLS-1$

		TransformerFactory trf = TransformerFactory.newInstance();
		Transformer t = trf.newTransformer(new StreamSource(getClass()
				.getResourceAsStream("about.xsl"))); //$NON-NLS-1$
		t.setParameter("language", language); //$NON-NLS-1$

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		t.transform(new StreamSource(xmlinputStream), new StreamResult(baos));

		textpanel.setContentType("text/html"); //$NON-NLS-1$
		textpanel.setText(new String(baos.toByteArray(), "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
		textpanel.setCaretPosition(0);

	}
}
