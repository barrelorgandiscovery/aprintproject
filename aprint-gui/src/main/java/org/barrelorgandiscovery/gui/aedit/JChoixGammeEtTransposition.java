package org.barrelorgandiscovery.gui.aedit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.scale.StorageScaleManager;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;
import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;
import org.barrelorgandiscovery.virtualbook.transformation.StorageTransformationManager;
import org.barrelorgandiscovery.virtualbook.transformation.TransformationManager;


public class JChoixGammeEtTransposition extends JDialog implements
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7312594058540894369L;

	private JPanel panel;

	private JComboBox choixgamme;

	private JComboBox choixtransposition;

	private JButton ok;

	private ScaleManager gm;

	private TransformationManager tm;

	public JChoixGammeEtTransposition(ScaleManager gammemanager,
			TransformationManager transpositionmanager) {

		this.gm = gammemanager;
		this.tm = transpositionmanager;

		panel = new JPanel();

		choixgamme = new JComboBox();
		choixtransposition = new JComboBox();

		choixgamme.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				updateIHM();
			}

		});

		ok = new JButton("OK");
		ok.setActionCommand("OK");
		ok.addActionListener(this);

		panel.add(choixgamme);
		panel.add(choixtransposition);
		panel.add(ok);

		getContentPane().add(panel);

		// remplissage de la liste des gammes

		String[] names = gm.getScaleNames();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			choixgamme.addItem(gm.getScale(name));
		}

		updateIHM();

		pack();
	}

	private void updateIHM() {

		if (choixgamme.getSelectedIndex() != -1) {
			Scale g = (Scale) choixgamme.getSelectedItem();

			// on récupère l'ensemble des transpositions associées à cette gamme
			ArrayList<AbstractTransformation> t = tm.findTransposition(Scale
					.getGammeMidiInstance(), g);

			choixtransposition.removeAllItems();
			for (Iterator iter = t.iterator(); iter.hasNext();) {
				LinearTransposition element = (LinearTransposition) iter.next();
				choixtransposition.addItem(element);
			}
		}

	}

	public LinearTransposition getTransposition() {
		return (LinearTransposition) choixtransposition.getSelectedItem();
	}

	public void actionPerformed(ActionEvent e) {
		if ("OK".equals(e.getActionCommand())) {
			setVisible(false);
		}
	}

	/**
	 * Routine de test.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			// Chargement des gamme et transpositions
			File rep = new File(
					"C:/Documents and Settings/Freydiere Patrice/workspace/APrint/gammes");

			FolderStreamStorage fis = new FolderStreamStorage(rep);

			ScaleManager gm = new StorageScaleManager(fis);

			TransformationManager tm = new StorageTransformationManager(fis, gm);

			JChoixGammeEtTransposition c = new JChoixGammeEtTransposition(gm,
					tm);
			c.setVisible(true);

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

}
