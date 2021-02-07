package org.barrelorgandiscovery.extensionsng.scanner.wizard.scanormerge;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensionsng.perfo.ng.extension.MachineExtension;
import org.barrelorgandiscovery.extensionsng.scanner.Messages;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JChooseFolderStep;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JMergeImagesStep;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JOutputFolderChooserStep;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JVideoInput;
import org.barrelorgandiscovery.extensionsng.scanner.wizardscan.JScanParameterStep;
import org.barrelorgandiscovery.extensionsng.scanner.wizardscan.JScanStep;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.wizard.Wizard;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Factory;

public class JScanOrMergeWizard extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8973318931575705458L;

	private IPrefsStorage ps;

	private Wizard wizard;

	private Repository2 repository;

	private IExtension[] extensions;

	public JScanOrMergeWizard(IPrefsStorage ps, Repository2 repository, IExtension[] extensions) throws Exception {
		this.ps = ps;
		this.repository = repository;
		this.extensions = extensions;
		initComponents();
	}

	protected void initComponents() throws Exception {

		// scan steps
		JOutputFolderChooserStep s = new JOutputFolderChooserStep(null, ps);
		s.setLabelOutputFolder(Messages.getString("JScanOrMergeWizard.0")); //$NON-NLS-1$
		JScanParameterStep p = new JScanParameterStep(s, ps, extensions);
		JScanStep scanStep = new JScanStep(p, s);

		JVideoInput videoFileInputStep = new JVideoInput(null);
		JMergeImagesStep mergeVideoStep = new JMergeImagesStep(videoFileInputStep, ps, repository);

		// merge steps

		// folder step for choosing folder
		JChooseFolderStep sf = new JChooseFolderStep(ps);

		JMergeImagesStep m = new JMergeImagesStep(sf, ps, repository);

		// this is the alternative step, to choose between methods
		JScanOrMergeStep scanOrmergeStep = new JScanOrMergeStep(null, Arrays.asList(sf, m),
				Arrays.asList(s, p, scanStep), Arrays.asList(videoFileInputStep, mergeVideoStep), ps);

		wizard = new Wizard(Arrays.asList(scanOrmergeStep), null);
		// give the context to step
		scanOrmergeStep.initWizard(wizard);

		setLayout(new BorderLayout());
		add(wizard, BorderLayout.CENTER);

		wizard.toFirst();
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		APrintProperties aPrintProperties = new APrintProperties(false);
		Repository2 rep = Repository2Factory.create(new Properties(), aPrintProperties);

		FilePrefsStorage p = new FilePrefsStorage(new File("c:\\temp\\wizardscan.properties")); //$NON-NLS-1$
		p.load();

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new JScanOrMergeWizard(p, rep, new IExtension[] { new MachineExtension() }),
				BorderLayout.CENTER);

		f.setSize(800, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
