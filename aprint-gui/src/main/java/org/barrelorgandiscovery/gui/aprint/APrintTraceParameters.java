package org.barrelorgandiscovery.gui.aprint;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.ui.tools.VerticalBagLayout;

/**
 * Boite de dialog permettant le choix de la taille du poinçon.
 * 
 * @author Freydiere Patrice
 */
public class APrintTraceParameters extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4661907892247533589L;

	private JTextField poinconlength;

	private double value = Double.NaN;

	public APrintTraceParameters(Frame window) {
		super(window);
		setTitle(Messages.getString("APrintTraceParameters.0")); //$NON-NLS-1$

		JPanel p = new JPanel(new VerticalBagLayout());

		JPanel parameters = new JPanel();
		parameters.setBorder(new TitledBorder(Messages
				.getString("APrintTraceParameters.1"))); //$NON-NLS-1$

		JLabel labelpoincon = new JLabel(Messages
				.getString("APrintTraceParameters.2")); //$NON-NLS-1$
		parameters.add(labelpoincon);

		poinconlength = new JTextField(Messages
				.getString("APrintTraceParameters.3")); //$NON-NLS-1$
		parameters.add(poinconlength);

		p.add(parameters);

		JPanel buttonpanel = new JPanel(new FlowLayout());

		JButton ok = new JButton(Messages.getString("APrintTraceParameters.4")); //$NON-NLS-1$
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				value = Double.parseDouble(poinconlength.getText());
				setVisible(false);
			}
		});

		buttonpanel.add(ok);

		p.add(buttonpanel);

		getContentPane().add(p);

		pack();

	}

	public double getValue() {
		return value;
	}

}
