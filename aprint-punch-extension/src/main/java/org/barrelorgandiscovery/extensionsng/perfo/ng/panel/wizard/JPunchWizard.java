package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.extensionsng.perfo.ng.optimizers.OptimizersRepository;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.atrace.PunchLayer;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.Wizard;
import org.barrelorgandiscovery.issues.IssueLayer;
import org.barrelorgandiscovery.prefs.DummyPrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult;

public class JPunchWizard extends JPanel implements Disposable {

	private ProcessingOptimizerEngine engine;

	private IPrefsStorage ps;

	private Wizard wizard;

	public JPunchWizard(PunchLayer pl, IssueLayer il, AsyncJobsManager jobManager, IPrefsStorage ps,
			JVirtualBookScrollableComponent pianoroll) throws Exception {

		assert ps != null;
		this.ps = ps;

		OptimizersRepository oRepository = new OptimizersRepository();

		engine = new ProcessingOptimizerEngine(null, pl, il, null, oRepository);

		engine.setAsyncManager(jobManager);

		StepChooseMachine stepChooseMachine = new StepChooseMachine(ps);
		StepPlanning splanning = new StepPlanning(oRepository, stepChooseMachine, engine, pianoroll, ps);
		splanning.setParentStep(stepChooseMachine);
		StepResume sresume = new StepResume(stepChooseMachine, pianoroll.getVirtualBook(), ps);
		sresume.setParentStep(splanning);

		List<Step> steps = new ArrayList<Step>();
		steps.add(stepChooseMachine);
		steps.add(splanning);
		steps.add(sresume);

		wizard = new Wizard(steps, null);
		// w.defineLastStepLabelName("Restart");
		wizard.setShowLastButton(false);

		setLayout(new BorderLayout());
		add(wizard, BorderLayout.CENTER);

	}

	
	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		if (size.getWidth() < 300) {
			size.setSize(300, size.getHeight());
		}
		return size;
	}
	
	public void setVirtualBook(VirtualBook vb) throws Exception {
		engine.changeVirtualBook(vb);
	}

	public void setScrollableVirtualBook(JVirtualBookScrollableComponent sc) {
		engine.setScrollableVirtualBook(sc);
	}

	
	/**
	 * Test method f
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure();

		JEditableVirtualBookComponent vbv = new JEditableVirtualBookComponent();
		vbv.activatePanOnMiddleButton();

		final VirtualBookResult vb = VirtualBookXmlIO.read(new File("C:\\Users\\use\\Dropbox\\APrint\\Books\\49\\carton_jerome.book")); //$NON-NLS-1$

		// final VirtualBookResult vb = VirtualBookXmlIO.read(new File("C:\\Users\\use\\Dropbox\\APrint\\Books\\52\\Folies Bergere.book")); //$NON-NLS-1$

		
		vbv.setPreferredSize(new Dimension(300, 300));
		vbv.setVirtualBook(vb.virtualBook);

		PunchLayer pl = new PunchLayer();
		pl.setVisible(true);
		vbv.addLayer(pl);
		pl.setOrigin(PunchLayer.ORIGIN_CENTER);

		IssueLayer il = new IssueLayer();
		vbv.addLayer(il);

		AsyncJobsManager jobManager = new AsyncJobsManager();

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		OptimizersRepository oRepository = new OptimizersRepository();

		ProcessingOptimizerEngine engine = new ProcessingOptimizerEngine(null, pl, il, null, oRepository);

		engine.setAsyncManager(jobManager);

		// define a virtualbook
		engine.changeVirtualBook(vb.virtualBook);

		StepChooseMachine stepChooseMachine = new StepChooseMachine(new DummyPrefsStorage());
		StepPlanning splanning = new StepPlanning(oRepository, stepChooseMachine, engine, vbv, new DummyPrefsStorage());
		splanning.setParentStep(stepChooseMachine);
		StepResume sresume = new StepResume(stepChooseMachine, vb.virtualBook, new DummyPrefsStorage());
		sresume.setParentStep(splanning);
		
		
		

		List<Step> steps = new ArrayList<Step>();
		steps.add(stepChooseMachine);
		steps.add(splanning);
		steps.add(sresume);

		Wizard w = new Wizard(steps, null);
		w.defineLastStepLabelName("Restart"); //$NON-NLS-1$
		w.setShowLastButton(false);

		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(w, BorderLayout.EAST);
		f.getContentPane().add(vbv, BorderLayout.CENTER);
		f.setSize(800, 500);
		f.setVisible(true);

	}

	@Override
	public void dispose() {
		if (engine != null) {
			engine.dispose();
		}
		if (wizard == null) {
			wizard.dispose();
		}
	}

}
