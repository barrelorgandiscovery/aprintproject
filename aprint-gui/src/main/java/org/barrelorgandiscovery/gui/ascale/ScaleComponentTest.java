package org.barrelorgandiscovery.gui.ascale;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.scale.StorageScaleManager;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;


/**
 * Test class for scale component 
 * 
 */
public class ScaleComponentTest extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4022856415553506484L;
	private ScaleComponent gc = new ScaleComponent();

	public ScaleComponentTest() {
		super();
		JScrollPane p = new JScrollPane(gc);
		p.setAutoscrolls(true);
		getContentPane().add(p);
		setSize(400, 600);
	}

	/**
	 * méthode de test unitaire pour le composant de gamme ...
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());
		
		ScaleComponentTest t = new ScaleComponentTest();

		// Lecture de gammes ...
		ScaleManager gm = new StorageScaleManager(
				new FolderStreamStorage(
						new File(
								"C:\\Documents and Settings\\Freydiere Patrice\\workspace\\APrint\\gammes"))); //$NON-NLS-1$

		t.gc.loadScale(gm.getScale("52 Limonaire")); //$NON-NLS-1$
		// t.gc.setScale(2.0);
		
		t.setVisible(true);
		t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}
