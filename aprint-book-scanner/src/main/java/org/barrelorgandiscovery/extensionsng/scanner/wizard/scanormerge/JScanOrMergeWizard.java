package org.barrelorgandiscovery.extensionsng.scanner.wizard.scanormerge;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JChooseFolderStep;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JMergeImagesStep;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JOutputFolderChooserStep;
import org.barrelorgandiscovery.extensionsng.scanner.wizardscan.JScanParameterStep;
import org.barrelorgandiscovery.extensionsng.scanner.wizardscan.JScanStep;
import org.barrelorgandiscovery.gui.wizard.Wizard;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

public class JScanOrMergeWizard extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8973318931575705458L;

	private IPrefsStorage ps;

	private Wizard wizard;

	public JScanOrMergeWizard(IPrefsStorage ps) throws Exception {
		this.ps = ps;
		initComponents();
	}

	protected void initComponents() throws Exception {

		// scan steps
		JOutputFolderChooserStep s = new JOutputFolderChooserStep(null, ps);
		s.setLabelOutputFolder("Choose Written Output Folder for images ...");
		JScanParameterStep p = new JScanParameterStep(s, ps);
		JScanStep scanStep = new JScanStep(p, s);

		// merge steps

		// folder step for choosing folder
		JChooseFolderStep sf = new JChooseFolderStep(ps);

		JMergeImagesStep m = new JMergeImagesStep(sf, ps);

		// this is the alternative step, to choose between methods
		JScanOrMergeStep scanOrmergeStep = new JScanOrMergeStep(null, Arrays.asList(sf, m),
				Arrays.asList(s, p, scanStep), ps);

		wizard = new Wizard(Arrays.asList(scanOrmergeStep), null);
		// give the context to step
		scanOrmergeStep.initWizard(wizard);

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
		f.getContentPane().add(new JScanOrMergeWizard(p), BorderLayout.CENTER);

		f.setSize(800, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
