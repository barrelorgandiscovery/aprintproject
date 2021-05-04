package org.barrelorgandiscovery.extensionsng.scanner.wizardscan;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensionsng.perfo.ng.extension.MachineExtension;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JOutputFolderChooserStep;
import org.barrelorgandiscovery.gui.wizard.Wizard;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

public class JScanWizard extends JPanel {

	private IPrefsStorage ps;

	private Wizard wizard;
	private IExtension[] extensions;

	public JScanWizard(IPrefsStorage ps, IExtension[] extensions) throws Exception {
		this.ps = ps;
		this.extensions = extensions;
		initComponents();
	}

	protected void initComponents() throws Exception {

		JOutputFolderChooserStep s = new JOutputFolderChooserStep(null, ps);
		JScanParameterStep p = new JScanParameterStep(s, ps, extensions);
		JScanStep ss = new JScanStep(p, s);

		wizard = new Wizard(Arrays.asList(s, p, ss), null);

		setLayout(new BorderLayout());
		add(wizard, BorderLayout.CENTER);

		wizard.toFirst();
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		FilePrefsStorage p = new FilePrefsStorage(new File("c:\\temp\\wizardscan.properties"));
		p.load();

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new JScanWizard(p, new IExtension[] { new MachineExtension() }), BorderLayout.CENTER);

		f.setSize(800, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
