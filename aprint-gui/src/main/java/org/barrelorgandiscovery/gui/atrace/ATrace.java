package org.barrelorgandiscovery.gui.atrace;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tracetools.ga.GeneticOptimizer;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.io.MidiIO;


public class ATrace extends JFrame {

	private static final Logger logger = Logger.getLogger(ATrace.class);

	private VirtualBook carton;

	private PunchLayer pl = new PunchLayer();
	
	private JVirtualBookScrollableComponent cartonv = new JVirtualBookScrollableComponent(pl);

	public ATrace(VirtualBook carton) {
		this.carton = carton;

		cartonv.setVirtualBook(carton);
		
		getContentPane().add(cartonv, BorderLayout.CENTER);

		JMenuBar menubar = new JMenuBar();
		JMenu fichier = new JMenu("Fichier");
		menubar.add(fichier);

		JMenuItem exporter = new JMenuItem("Exporter ..");
		fichier.add(exporter);

		exporter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Sélection du fichier dans lequel exporter
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Punch file", "punch");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showSaveDialog(ATrace.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// Enregistrer la liste des punches ...
					try {
						PunchFile.savePunch(chooser.getSelectedFile(),
								pl.getPunch());
						JMessageBox.showMessage(ATrace.this, "Export réalisé");
					} catch (Exception ex) {
						logger.error("saving punches", ex);
						JMessageBox
								.showMessage(ATrace.this, "Erreur lors de la sauvegarde du fichier");
					}
				}
			}
		});

		setJMenuBar(menubar);

	}

	public void setPunch(Punch[] punches) {
		pl.setPunch(punches);
		cartonv.repaint();
	}

	public static void main(String[] args) {
		try {

			BasicConfigurator.configure();

			// Chargement du fichier Midi + Gamme ...

			VirtualBook c = MidiIO
					.readCarton(new File(
							"C:\\Documents and Settings\\Freydiere Patrice\\Bureau\\La_Souris_Noire_20080212.MID"));

			ATrace t = new ATrace(c);

			// SimpleTspOptimizer op = new SimpleTspOptimizer();
			GeneticOptimizer op = new GeneticOptimizer();

			OptimizerResult res = op.optimize(c);

			t.setPunch(res.result);

			t.setSize(600, 600);
			t.setVisible(true);

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

}
