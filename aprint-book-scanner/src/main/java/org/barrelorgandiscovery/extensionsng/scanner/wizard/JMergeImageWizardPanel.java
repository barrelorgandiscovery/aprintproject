package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.wizard.Wizard;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Factory;

public class JMergeImageWizardPanel extends JPanel {

	private IPrefsStorage ps;

	private Wizard wizard;

	private Repository2 repository;

	public JMergeImageWizardPanel(IPrefsStorage ps, Repository2 repository) throws Exception {
		this.ps = ps;
		this.repository = repository;
		initComponents();
	}

	protected void initComponents() throws Exception {

		// folder step for choosing folder
		JChooseFolderStep s = new JChooseFolderStep(ps);

		JMergeImagesStep m = new JMergeImagesStep(s, ps, repository);

		wizard = new Wizard(Arrays.asList(s, m), null);

		setLayout(new BorderLayout());
		add(wizard, BorderLayout.CENTER);

		wizard.toFirst();
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		FilePrefsStorage p = new FilePrefsStorage(new File("c:\\temp\\preferencesStorage.properties"));
		p.load();

		APrintProperties aPrintProperties = new APrintProperties(false);
		Repository2 repository = Repository2Factory.create(new Properties(), aPrintProperties);

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new JMergeImageWizardPanel(p, repository), BorderLayout.CENTER);

		f.setSize(800, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
