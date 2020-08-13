package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.controlling.PunchCommandPanel;
import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CutToCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlanIO;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.StatisticVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.XYCommand;
import org.barrelorgandiscovery.gui.aprintng.APrintNGInternalFrame;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.ui.tools.VFSTools;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import com.jeta.forms.components.panel.FormPanel;

public class StepResume extends JPanel implements Step {

	private static Logger logger = Logger.getLogger(StepResume.class);

	private JButton export;
	private JButton punch;

	private JEditorPane resume;

	private VirtualBook vb;

	private StepChooseMachine sm; // for opening the machine

	private IPrefsStorage ps;

	public StepResume(StepChooseMachine sm, VirtualBook vb, IPrefsStorage ps) throws Exception {

		assert ps != null;
		this.ps = ps;
		initComponents();
		this.vb = vb;
		assert sm != null;
		this.sm = sm;
	}

	protected void initComponents() throws Exception {

		setLayout(new BorderLayout());
		FormPanel p = new FormPanel(getClass().getResourceAsStream("resume.jfrm")); //$NON-NLS-1$
		add(p, BorderLayout.CENTER);

		export = (JButton) p.getButton("export"); //$NON-NLS-1$
		export.setText(Messages.getString("StepResume.2")); //$NON-NLS-1$
		export.setAction(new ExportAction());
		export.setIcon(new ImageIcon(getClass().getResource("filesave.png"))); //$NON-NLS-1$

		punch = (JButton) p.getButton("punch"); //$NON-NLS-1$
		punch.setText(Messages.getString("StepResume.4")); //$NON-NLS-1$
		punch.setAction(new PunchAction());
		punch.setIcon(new ImageIcon(PunchCommandPanel.class.getResource("smallpunch.png"))); //$NON-NLS-1$

		JLabel lblexport = (JLabel) p.getLabel("lblexport");//$NON-NLS-1$
		lblexport.setText(Messages.getString("StepResume.0")); //$NON-NLS-1$

		JLabel lblpunch = (JLabel) p.getLabel("lblpunch");//$NON-NLS-1$
		lblpunch.setText(Messages.getString("StepResume.1")); //$NON-NLS-1$

		resume = new JEditorPane("text/html", ""); //$NON-NLS-1$ //$NON-NLS-2$
		resume.setEditable(false);

		p.getFormAccessor().replaceBean("planningresume", resume); //$NON-NLS-1$

		invalidate();

	}

	public String getId() {
		return "planready"; //$NON-NLS-1$
	}

	private Step parent;

	public Step getParentStep() {
		return parent;
	}

	public void setParentStep(Step parent) {
		this.parent = parent;
	}

	public String getLabel() {
		return Messages.getString("StepResume.9"); //$NON-NLS-1$
	}

	private PunchPlan currentPlan = null;

	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {

		StepPlanningState s = allStepsStates.getPreviousStateImplementing(this, StepPlanningState.class);

		currentPlan = s.punchPlan;

		updateResume();
	}

	private void updateResume() throws Exception {

		StringBuilder sb = new StringBuilder();
		sb.append(Messages.getString("StepResume.10") + Messages.getString("StepResume.11")); //$NON-NLS-1$ //$NON-NLS-2$

		StatisticVisitor s = new StatisticVisitor(true);

		s.visit(currentPlan);

		sb.append(s.getReport());

		resume.setText(sb.toString());
	}

	public Serializable unActivateAndGetSavedState() throws Exception {
		return null;
	}

	public boolean isStepCompleted() {
		return true;
	}

	public String getDetails() {
		return Messages.getString("StepResume.12"); //$NON-NLS-1$
	}

	class ExportAction extends AbstractAction {

		private static final String GCODE_EXTENSION = "gcode";

		public ExportAction() {
			super(Messages.getString("StepResume.13")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			try {
				if (currentPlan == null) {
					JMessageBox.showMessage(StepResume.this, Messages.getString("StepResume.14")); //$NON-NLS-1$
					return;
				}

				APrintFileChooser fc = new APrintFileChooser();
				fc.setMultiSelectionEnabled(false);
				fc.setFileFilter(new VFSFileNameExtensionFilter("GCode File", GCODE_EXTENSION)); //$NON-NLS-1$ //$NON-NLS-2$

				int f = fc.showSaveDialog(StepResume.this);
				if (f == APrintFileChooser.APPROVE_OPTION) {
					AbstractFileObject fileToSave = fc.getSelectedFile();

					fileToSave = VFSTools.ensureExtensionIs(fileToSave, GCODE_EXTENSION);

					// export plan

					PunchBookAndPlan reversed = reverseToHaveTheReferenceUp(vb, currentPlan);

					OutputStream os = fileToSave.getOutputStream();
					try {
						OutputStreamWriter w = new OutputStreamWriter(os);
						try {

							PunchPlanIO.exportToGRBL(w, reversed.punchplan,
									sm.getSelectedMachine().createNewGCodeCompiler(sm.getMachineParameters()));

						} finally {
							w.close();
						}
					} finally {
						os.close();
					}
					JMessageBox.showMessage(StepResume.this, Messages.getString("StepResume.15") //$NON-NLS-1$
							+ fileToSave + Messages.getString("StepResume.16")); //$NON-NLS-1$
				}

			} catch (Exception ex) {
				logger.error("error in exporting the file :" + ex.getMessage(), //$NON-NLS-1$
						ex);
				JMessageBox.showError(StepResume.this, ex);
			}

		}
	}

	// Punch frame
	class PunchFrameInternalFrame extends APrintNGInternalFrame {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3918107214219687716L;

		/**
		 * punch command panel
		 */
		private PunchCommandPanel punchCommandPanel;

		public PunchFrameInternalFrame(IPrefsStorage prefsStorage, MachineControl machineControl, VirtualBook vb)
				throws Exception {
			super(prefsStorage, Messages.getString("StepResume.3")); //$NON-NLS-1$

			getContentPane().setLayout(new BorderLayout());

			PunchBookAndPlan reverseToHaveTheReferenceUp = reverseToHaveTheReferenceUp(vb, currentPlan);
			assert reverseToHaveTheReferenceUp != null;
			assert reverseToHaveTheReferenceUp.punchplan != null;
			assert reverseToHaveTheReferenceUp.virtualBook != null;

			// prepend the origin displacement
			DisplacementCommand origin = new DisplacementCommand(0, 0);

			PunchPlan planCopy = new PunchPlan(reverseToHaveTheReferenceUp.punchplan);
			planCopy.getCommandsByRef().add(0, origin);

			PunchCommandPanel punchCommandPanel = new PunchCommandPanel(planCopy,
					reverseToHaveTheReferenceUp.virtualBook, ps);

			punchCommandPanel.setMachineControl(machineControl);
			getContentPane().add(punchCommandPanel);

			this.punchCommandPanel = punchCommandPanel;

			toggleDirty();

		}

		@Override
		public void dispose() {
			punchCommandPanel.dispose();
			super.dispose();
		}

	}

	class PunchAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5799435463991897625L;

		@Override
		public void actionPerformed(ActionEvent e) {
			try {

				logger.debug("creating the window"); //$NON-NLS-1$

				AbstractMachineParameters machineParameters = sm.getMachineParameters();
				AbstractMachine machine = machineParameters.createAssociatedMachineInstance();

				logger.debug("open machine");
				MachineControl machineControl = machine.open(machineParameters);

				logger.debug("create window");
				APrintNGInternalFrame aif = new PunchFrameInternalFrame(ps, machineControl, vb);

				aif.setSize(800, 600);

				aif.setVisible(true);

			} catch (Exception ex) {
				logger.error("error launching the punch, :" + ex.getMessage(), ex); //$NON-NLS-1$
				JMessageBox.showError(null, ex);
			}
		}
	}

	@Override
	public Icon getPageImage() {
		return null;
	}

	/**
	 * in case the reference is reverted, this switch the plan
	 * 
	 * @param vb
	 * @param punchplan
	 * @return
	 * @throws Exception
	 */
	public static PunchBookAndPlan reverseToHaveTheReferenceUp(VirtualBook vb, PunchPlan punchplan) throws Exception {
		Scale scale = vb.getScale();
		assert scale != null;
		PunchBookAndPlan retvalue = new PunchBookAndPlan();
		if (!scale.isPreferredViewedInversed()) {
			retvalue.punchplan = punchplan;
			retvalue.virtualBook = vb;
			return retvalue;
		}

		logger.debug("reverse the elements");
		assert scale.isPreferredViewedInversed();

		// new scale with preferred view inversed modified
		Scale modifiedScale = new Scale(scale.getName(), scale.getWidth(), scale.getIntertrackHeight(),
				scale.getTrackWidth(), scale.getFirstTrackAxis(), scale.getTrackNb(), scale.getTracksDefinition(),
				scale.getPipeStopGroupList(), scale.getSpeed(), scale.getConstraints(), scale.getInformations(),
				scale.getState(), scale.getContact(), scale.getRendering(), false, scale.isBookMovingRightToLeft(),
				scale.getAllProperties());

		VirtualBook newReversed = new VirtualBook(modifiedScale);

		Long maxTimeStamp = vb.getHolesCopy().stream().map((Hole h) -> h.getTimestamp() + h.getTimeLength())
				.reduce((a, b) -> Math.max(a, b)).get();

		// long maxTimestamp =
		for (Hole h : vb.getHolesCopy()) {
			Hole nh = new Hole(h.getTrack(), maxTimeStamp - h.getTimestamp() - h.getTimeLength(), h.getTimeLength());
			newReversed.addHole(nh);
		}

		// clone and reverse the punchplan
		PunchPlan modifiedPunchPlan = new PunchPlan();

		Double maxX = punchplan.getCommandsByRef().stream().map((Command i) -> {
			if (i instanceof XYCommand) {
				return ((XYCommand) i).getX();
			}
			return 0.0;
		}).reduce((a, b) -> Math.max(a, b)).get();

		List<Command> originalCommands = punchplan.getCommandsByRef();

		List<Command> reversedCommands = new ArrayList<Command>();
		for (Command c : originalCommands) {
			if (c instanceof XYCommand) {
				if (c instanceof DisplacementCommand) {

					DisplacementCommand displacementCommand = (DisplacementCommand) c;
					reversedCommands.add(
							new DisplacementCommand(maxX - displacementCommand.getX(), displacementCommand.getY()));

				} else if (c instanceof PunchCommand) {
					PunchCommand displacementCommand = (PunchCommand) c;
					reversedCommands
							.add(new PunchCommand(maxX - displacementCommand.getX(), displacementCommand.getY()));

				} else if (c instanceof CutToCommand) {
					CutToCommand cutcommand = (CutToCommand)c;
					reversedCommands.add(new CutToCommand(
							maxX - cutcommand.getX(),
							cutcommand.getY(), 
							cutcommand.getPowerFactor(),
							cutcommand.getSpeedFactor()
							));
				} else {
					throw new Exception("unsupported command type, implementation error :" + c);
				}

			} else {
				reversedCommands.add(c);
			}
		}

		// reverse all commands
		while (reversedCommands.size() > 0) {
			Command c = reversedCommands.get(reversedCommands.size() - 1);
			reversedCommands.remove(reversedCommands.size() - 1);
			modifiedPunchPlan.getCommandsByRef().add(c);
		}

		retvalue.virtualBook = newReversed;
		retvalue.punchplan = modifiedPunchPlan;
		retvalue.hasBeenChanged = true;

		return retvalue;
	}

	public static class PunchBookAndPlan {
		public VirtualBook virtualBook;
		public PunchPlan punchplan;
		public boolean hasBeenChanged = false;
	}

}
