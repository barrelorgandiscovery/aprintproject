package org.barrelorgandiscovery.gui.repository;

import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.barrelorgandiscovery.instrument.Instrument;

import com.jeta.forms.components.panel.FormPanel;

/**
 * Panel for displaying informations about an instrument
 * 
 * @author Freydiere Patrice
 * 
 */
public class JInstrumentForm extends JPanel {

	private JLabel label;
	private JLabel insPicture;

	public JInstrumentForm() throws Exception {
		initComponents();
	}

	private void initComponents() throws Exception {
		FormPanel p = new FormPanel(getClass().getResourceAsStream(
				"instrumentForm.jfrm")); //$NON-NLS-1$

		this.setLayout(new BorderLayout());
		this.removeAll();
		this.add(p, BorderLayout.CENTER);

		label = p.getLabel("instrumentName"); //$NON-NLS-1$

		insPicture = p.getLabel("instrumentPicture"); //$NON-NLS-1$

	}

	public void setInstrument(Instrument instrument) {

		label.setText(instrument.getName());
		insPicture.setText(""); //$NON-NLS-1$

		Image thumbnail = instrument.getThumbnail();
		if (thumbnail != null) {
			insPicture.setIcon(new ImageIcon(thumbnail));
		} else {
			insPicture.setIcon(null);
		}
	}

}
