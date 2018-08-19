package aprintextensions.fr.freydierepatrice.perfo.gerard;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.barrelorgandiscovery.tools.SwingUtils;

/**
 * Dialogue permettant la saisie des paramètres de perforation
 * 
 * @author Freydiere Patrice
 * 
 */
public class JPerfoExtensionParameters extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2226577522035308816L;

	private PerfoExtensionParameters perfoParameters;

	private boolean isCanceled = true;

	private JPerfoExtensionParameters(Frame owner,
			PerfoExtensionParameters initParameters) {
		super(owner);
		setTitle("Parametres pour le perçage");

		assert initParameters != null;

		this.perfoParameters = initParameters;

		// construction de la fenetre ...
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Parametres"));

		GridBagLayout gl = new GridBagLayout();
		p.setLayout(gl);

		JSpinner splargeur = new JSpinner(new SpinnerNumberModel(
				perfoParameters.poinconsize, 1.0, 15.0, 0.1));
		splargeur.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				perfoParameters.poinconsize = (((Number) s.getValue())
						.doubleValue());
			}
		});
		splargeur.setEditor(new JSpinner.NumberEditor(splargeur, "0.0"));

		p.add(splargeur, constructGridBagContraint(1, 1));
		p.add(new JLabel("Longueur du poinçon (dans le sens du carton) (mm)"),
				constructGridBagContraint(0, 1));

		JSpinner spheight = new JSpinner(new SpinnerNumberModel(
				perfoParameters.poinconheight, 1.0, 15.0, 0.1));

		spheight.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				perfoParameters.poinconheight = (((Number) s.getValue())
						.doubleValue());
			}
		});
		spheight.setEditor(new JSpinner.NumberEditor(spheight, "0.0"));

		p.add(spheight, constructGridBagContraint(1, 2));
		p.add(new JLabel("Hauteur du poinçon (dans le sens des pistes) (mm)"),
				constructGridBagContraint(0, 2));

		JSpinner spminimumLength = new JSpinner(new SpinnerNumberModel(
				perfoParameters.minimum_length_for_two_punch, 1.0, 15.0, 0.1));

		spminimumLength.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				perfoParameters.minimum_length_for_two_punch = (((Number) s
						.getValue()).doubleValue());
			}
		});
		spminimumLength.setEditor(new JSpinner.NumberEditor(spminimumLength,
				"0.0"));

		p.add(spminimumLength, constructGridBagContraint(1, 3));
		p.add(new JLabel("Distance minimum à respecter en fin de trous (mm):"),
				constructGridBagContraint(0, 3));

		JSpinner spChevauchement = new JSpinner(new SpinnerNumberModel(
				this.perfoParameters.avancement, 0.0, 15.0, 0.1));

		spChevauchement.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				perfoParameters.avancement = (((Number) s.getValue())
						.doubleValue());
			}
		});
		spChevauchement.setEditor(new JSpinner.NumberEditor(spChevauchement,
				"0.0"));

		p.add(spChevauchement, constructGridBagContraint(1, 4));
		p.add(new JLabel("Avancement :"), constructGridBagContraint(0, 4));

		JSpinner spPageSize = new JSpinner(new SpinnerNumberModel(
				perfoParameters.page_size, 1.0, 300.0, 1.0));
		spPageSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				perfoParameters.page_size = (((Number) s.getValue())
						.doubleValue());
			}
		});
		spPageSize.setEditor(new JSpinner.NumberEditor(spPageSize, "0.0"));

		p.add(spPageSize, constructGridBagContraint(1, 5));
		p.add(new JLabel("Taille de la page de perçage (mm):"),
				constructGridBagContraint(0, 5));

		getContentPane().add(p, BorderLayout.CENTER);

		// ajout du bouton ...
		JButton btnok = new JButton("Ok");
		btnok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isCanceled = false;
				setVisible(false);
			}
		});

		JPanel pb = new JPanel();
		pb.add(btnok);

		getContentPane().add(pb, BorderLayout.SOUTH);

		setSize(400, 300);

	}

	private GridBagConstraints constructGridBagContraint(int x, int y) {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = x;
		gridBagConstraints.gridy = y;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		return gridBagConstraints;
	}

	public static void main(String[] args) throws Exception {
		System.out
				.println(editParameters(null, new PerfoExtensionParameters()));
	}

	public static PerfoExtensionParameters editParameters(Frame owner,
			PerfoExtensionParameters initParams) {
		JPerfoExtensionParameters perfoExtensionParameters = new JPerfoExtensionParameters(
				null, initParams);

		perfoExtensionParameters.setModal(true);
		SwingUtils.center(perfoExtensionParameters);

		perfoExtensionParameters.setVisible(true);
		if (perfoExtensionParameters.isCanceled)
			return null;

		return perfoExtensionParameters.perfoParameters;

	}

}
