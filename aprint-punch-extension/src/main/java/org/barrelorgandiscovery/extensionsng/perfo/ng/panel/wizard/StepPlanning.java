package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.gui.PunchLayer;
import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.StatisticVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.optimizers.OptimizersRepository;
import org.barrelorgandiscovery.extensionsng.perfo.ng.tools.ClassLoaderObjectsPrefsStorage;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.issues.IssueSelector;
import org.barrelorgandiscovery.gui.issues.JIssuePresenter;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.issues.IssueHole;
import org.barrelorgandiscovery.issues.IssuesConstants;
import org.barrelorgandiscovery.optimizers.Optimizer;
import org.barrelorgandiscovery.optimizers.OptimizerResult;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.prefs.PrefixedNamePrefsStorage;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.ui.animation.InfiniteProgressPanel;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import com.jeta.forms.components.panel.FormPanel;
import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class StepPlanning extends JPanel implements Step, Disposable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 768126798467269246L;

	private static Logger logger = LogManager.getLogger(StepPlanning.class);

	private OptimizersRepository optimizers;

	private JPanel strategypanel;

	private PropertySheetPanel ppPanel;

	private StepChooseMachine sm;

	private ProcessingOptimizerEngine processingEngine;

	// private JConsole informationConsole;
	private ProgressPanelWithText progress;

	private PunchPlan currentPunchPlan;

	private JIssuePresenter issuePresenter;

	private StepStatusChangedListener stepStatusChangedListener;

	private JVirtualBookScrollableComponent pianoroll;

	private InfiniteProgressPanel infiniteprogresspanel = new InfiniteProgressPanel(null, 20, 0.5f, 0.5f);

	private IPrefsStorage rootPrefsStorage;

	private AtomicBoolean userHasBeenAskedForSmallHoles = new AtomicBoolean(false);

	public StepPlanning(final OptimizersRepository optimizers, final StepChooseMachine sm,
			final ProcessingOptimizerEngine ppt, final JVirtualBookScrollableComponent pianoroll,
			final IPrefsStorage prefsStorage) throws Exception {
		assert optimizers != null;
		assert prefsStorage != null;

		this.optimizers = optimizers;
		this.sm = sm;
		this.processingEngine = ppt;
		assert pianoroll != null;
		this.pianoroll = pianoroll;

		this.rootPrefsStorage =  prefsStorage; 

		ppt.setProcessingOptimizerEngineProgress(new ProcessingOptimizerEngineCallBack() {

			@Override
			public void progressUpdate(final double percentage, final String message) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {

							logTextInformation(String.format("%1$tT - %2$d  %3$s ", new Date(), //$NON-NLS-1$
									(int) (percentage * 100), message));
							StepPlanning.this.pianoroll.repaint();

							progress.setProgress(percentage);
						}
					});

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}

			@Override
			public void processEnded(final OptimizerResult result) {
				try {

					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {

							try {

								logTextInformation(Messages.getString("StepPlanning.1")); //$NON-NLS-1$

								OptimizedObject[] punches = result.result;

								AbstractMachine machine = sm.getSelectedMachine();
								assert machine != null;

								OptimizersRepository o = new OptimizersRepository();
								PunchPlan pp = o.createDefaultPunchPlanFromOptimizeResult(sm.getSelectedMachine(),
										sm.getMachineParameters(), punches);

								definePunchPlanning(pp);

								logTextInformation(Messages.getString("StepPlanning.2")); //$NON-NLS-1$

								StatisticVisitor v = new StatisticVisitor(false);
								v.visit(pp);
								logTextInformation(v.getReport().toString());

								issuePresenter.loadIssues(result.holeerrors);
								PunchLayer punchLayer = processingEngine.getPunchLayer();
								punchLayer.setOptimizedObject(result.result);

								final VirtualBook currentvb = processingEngine.getVirtualBook();
								processingEngine.getIssueLayer().setIssueCollection(result.holeerrors, currentvb);

								infiniteprogresspanel.stop();
								StepPlanning.this.pianoroll.repaint();
								if (stepStatusChangedListener != null)
									stepStatusChangedListener.stepStatusChanged();

								IssueCollection issueCollection = result.holeerrors;

								// if there are errors linked to

								if (hasTooSmallHoleErrors(issueCollection) && !userHasBeenAskedForSmallHoles.get()) {
									userHasBeenAskedForSmallHoles.set(true);
									int ret = JOptionPane.showConfirmDialog(StepPlanning.this,
											Messages.getString("StepPlanning.0") //$NON-NLS-1$
													+ Messages.getString("StepPlanning.5"), //$NON-NLS-1$
											Messages.getString("StepPlanning.6"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
									if (ret == JOptionPane.YES_OPTION) {
										// modify the book
										logger.debug("change the hole sizes"); //$NON-NLS-1$
										assert pianoroll instanceof JEditableVirtualBookComponent;
										JEditableVirtualBookComponent c = (JEditableVirtualBookComponent) pianoroll;
										c.startEventTransaction();
										try {

											List<Hole> holes = currentvb.getHolesCopy();
											for (Hole h : holes) {
												if (pianoroll.timestampToMM(h.getTimeLength()) < punchLayer
														.getPunchWidth()) {
													currentvb.addHole(new Hole(h.getTrack(), h.getTimestamp(),
															pianoroll.MMToTime(punchLayer.getPunchWidth())));
													currentvb.removeHole(h);
												}
											}

										} finally {
											c.endEventTransaction();
										}
										startNewComputation();
									}

								}

							} catch (Exception ex) {
								logger.error(Messages.getString("StepPlanning.3") //$NON-NLS-1$
										+ ex.getMessage(), ex);
							}
						}

					});
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}

			}
		});

		initComponents();
	}

	protected void initComponents() throws Exception {

		setLayout(new BorderLayout());

		FormPanel panel = new FormPanel(getClass().getResourceAsStream("planning.jfrm")); //$NON-NLS-1$

		// panel for the optimizer selection
		this.strategypanel = (JPanel) panel.getFormAccessor().getComponentByName("strategypanel"); //$NON-NLS-1$

		JLabel lblchoixstrategy = (JLabel) panel.getFormAccessor().getComponentByName("lblstrategychoice");//$NON-NLS-1$
		lblchoixstrategy.setText(Messages.getString("StepPlanning.4")); //$NON-NLS-1$

		// vertical strategy
		strategypanel.setLayout(new BoxLayout(strategypanel, BoxLayout.Y_AXIS));

		// optimizing properties definition
		this.ppPanel = new PropertySheetPanel();
		ppPanel.setMinimumSize(new Dimension(100, 300));
		ppPanel.setPreferredSize(new Dimension(200, 300));
		ppPanel.setDescriptionVisible(true);
		
		panel.getFormAccessor().replaceBean("parameters", ppPanel); //$NON-NLS-1$

		progress = new ProgressPanelWithText();

		panel.getFormAccessor().replaceBean("informations", progress); //$NON-NLS-1$

		this.issuePresenter = new JIssuePresenter(this, false, false);
		issuePresenter.setIssueSelectionListener(new IssueSelector(processingEngine.getIssueLayer(), pianoroll));

		JRootPane jrp = new JRootPane();
		jrp.setContentPane(issuePresenter);
		jrp.setGlassPane(infiniteprogresspanel);

		panel.getFormAccessor().replaceBean("issues", jrp); //$NON-NLS-1$

		JScrollPane sp = new JScrollPane(panel);
		sp.setAutoscrolls(true);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		add(sp, BorderLayout.CENTER);
		panel.invalidate();
		sp.invalidate();
	}

	@Override
	public String getId() {
		return "planning"; //$NON-NLS-1$
	}

	private Step parent;

	@Override
	public Step getParentStep() {
		return parent;
	}

	public void setParentStep(Step parent) {
		this.parent = parent;
	}

	public String getLabel() {
		return Messages.getString("StepPlanning.10"); //$NON-NLS-1$
	}

	/**
	 * called when activated
	 */
	protected void adjustGUIfromPreviousStep() {

		// grab the previous machine

		// if previous machine changed, clear the current punchplan

	}

	private RelaunchOptimizer relaunchEventListener = new RelaunchOptimizer();

	private final class RelaunchOptimizer implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			startNewComputation();
		}
	}

	private void startNewComputation() {
		logTextInformation(Messages.getString("StepPlanning.11")); //$NON-NLS-1$
		try {

			// save parameters
			saveUserParameters(currentOptimizerClass, currentOptimizerParameters);

			processingEngine.changeOptimizerParameters(currentOptimizerParameters);

			definePunchPlanning(null);

			if (stepStatusChangedListener != null)
				stepStatusChangedListener.stepStatusChanged();

			// informationConsole.clearScreen();
			progress.reset();
			

		} catch (Exception ex) {

			logger.error("error in changing optimizer parameters :" //$NON-NLS-1$
					+ ex.getMessage(), ex);
		} finally {
			if (!infiniteprogresspanel.isStarted())
				infiniteprogresspanel.start(Messages.getString("StepPlanning.13")); //$NON-NLS-1$
		}
	}

	/**
	 * radio button for the optimizer
	 * 
	 * @author pfreydiere
	 * 
	 */
	private class JOptimizerRadioButton extends JRadioButton {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1775818254230613406L;

		private Class<?> optimizerClass = null;;

		public JOptimizerRadioButton(String text) {
			super(text);
		}

		public void setOptimizerClass(Class<?> optimizerClass) {
			this.optimizerClass = optimizerClass;
		}

		public Class<?> getOptimizerClass() {
			return optimizerClass;
		}

		private Serializable optimizerParameters;

		public void setOptimizerParameters(Serializable optimizerParameters) {
			this.optimizerParameters = optimizerParameters;
		}

		public Serializable getOptimizerParameters() {
			return optimizerParameters;
		}

	}
	
	private IPrefsStorage currentMachinePrefsStorage = null;

	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {

		// reset
		userHasBeenAskedForSmallHoles.set(false);

		AbstractMachine machine = sm.getSelectedMachine();
		assert machine != null;
		
		// adjust the preference storage,
		String prefsPrefix = "perfoextensionoptimizers";
		AbstractMachine selectedMachine = sm.getSelectedMachine();
		if (selectedMachine != null) {
			prefsPrefix = StringTools.toHex(selectedMachine.getTitle());
		}

		this.currentMachinePrefsStorage = new PrefixedNamePrefsStorage(prefsPrefix, rootPrefsStorage); // $NON-NLS-1$
		try {
		this.currentMachinePrefsStorage.load();
		} catch(Exception ex) {
			logger.debug("error in loading preferences " + ex.getMessage(), ex);
		}
		currentOptimizerParameters = null;
		currentOptimizerClass = null;
		definePunchPlanning(currentPunchPlan);

		infiniteprogresspanel.stop();

		processingEngine.getPunchLayer().setVisible(true);

		// reinstall parameter if the state is given
		StepPlanningState s = (StepPlanningState) state;
		if (s != null) {
			if (machine.isSameModelAs(s.machine)) {
				// keep parameters
				currentOptimizerParameters = s.parameters;
				currentOptimizerClass = s.optimizerClass;
				definePunchPlanning(s.punchPlan);
			}
		}

		List<Class> listAvailableOptimizersForMachine = this.optimizers.listAvailableOptimizersForMachine(machine);

		// add all Optimizers

		this.strategypanel.removeAll();

		ButtonGroup buttonGroup = new ButtonGroup();

		ClassLoaderObjectsPrefsStorage ops = new ClassLoaderObjectsPrefsStorage(currentMachinePrefsStorage);

		JOptimizerRadioButton firstStrategyButton = null;

		logger.debug("instanciate optimizers"); //$NON-NLS-1$
		for (Class c : listAvailableOptimizersForMachine) {

			logger.debug("install strategy " + c + " in panel");

			assert Optimizer.class.isAssignableFrom(c);
			Optimizer o = (Optimizer) c.newInstance();
			
			JOptimizerRadioButton jRadioButton = new JOptimizerRadioButton(o.getTitle());

			if (firstStrategyButton == null /* && c == NoReturnPunchConverterOptimizer.class*/) {
				firstStrategyButton = jRadioButton;
			}

			jRadioButton.setOptimizerClass(c);

			Serializable instanciatedParametersForOptimizer = optimizers.instanciateParametersForOptimizer(c);

			logger.debug("load stored parameters"); //$NON-NLS-1$
			try {

				Serializable storedObject = ops.loadObjectProperties(c.getSimpleName());

				if (storedObject != null && storedObject.getClass().getName().equals(instanciatedParametersForOptimizer.getClass().getName())) {
					instanciatedParametersForOptimizer = storedObject;
				}
			} catch (Exception ex) {
				logger.debug("fail to load optimizer parameters :" + ex.getMessage(), //$NON-NLS-1$
						ex);
			}

			jRadioButton.setOptimizerParameters(instanciatedParametersForOptimizer);

			buttonGroup.add(jRadioButton);

			if (currentOptimizerClass != null) {
				if (currentOptimizerClass.equals(c)) {
					jRadioButton.setSelected(true);
					// define the parmeters
					currentOptimizerParameters = instanciatedParametersForOptimizer;
				}
			}

			this.strategypanel.add(jRadioButton);
			jRadioButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						JOptimizerRadioButton o = ((JOptimizerRadioButton) e.getSource());
						if (o != null) {
							currentOptimizerClass = o.getOptimizerClass();
							currentOptimizerParameters = o.getOptimizerParameters();

							// save parameters

							saveUserParameters(currentOptimizerClass, currentOptimizerParameters);

							adjustGUIFromSelectedStrategy();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						logger.error(ex.getMessage(), ex);
					}
				}
			});
		}

		// clear console
		// informationConsole.clearScreen(); //$NON-NLS-1$
		progress.reset();

		stepStatusChangedListener = stepListener;
		stepListener.stepStatusChanged();

		// if no strategy selected,
		// select the fastest one

		if (firstStrategyButton != null) {
			try {
				firstStrategyButton.getModel().setSelected(true);

				currentOptimizerClass = firstStrategyButton.getOptimizerClass();
				currentOptimizerParameters = firstStrategyButton.getOptimizerParameters();

				adjustGUIFromSelectedStrategy();

			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error(ex.getMessage(), ex);
			}
		}

	}

	private Class currentOptimizerClass = null;
	private Serializable currentOptimizerParameters = null;

	protected void adjustGUIFromSelectedStrategy() throws Exception {

		if (currentOptimizerClass == null || currentOptimizerParameters == null) {
			logger.warn("implementation error"); //$NON-NLS-1$
			return;
		}

		// instanciate the BeanInfo

		assert currentOptimizerParameters != null;
		String biClassName = currentOptimizerParameters.getClass().getName() + "BeanInfo"; //$NON-NLS-1$

		Class biClass = Class.forName(biClassName);

		BeanBinder beanBinder = new BeanBinder(currentOptimizerParameters, ppPanel, (BeanInfo) biClass.newInstance());

		// replace at the end
		ppPanel.removePropertySheetChangeListener(relaunchEventListener);
		ppPanel.addPropertySheetChangeListener(relaunchEventListener);

		startNewComputation();

	}

	public Serializable unActivateAndGetSavedState() throws Exception {

		StepPlanningState sps = new StepPlanningState();
		sps.parameters = (Serializable) currentOptimizerParameters;
		sps.punchPlan = currentPunchPlan;
		sps.optimizerClass = currentOptimizerClass;
		sps.machine = sm.getSelectedMachine();

		definePunchPlanning(currentPunchPlan);
		
		saveUserParameters(currentOptimizerClass, sps.parameters);

		return sps;
	}

	public boolean isStepCompleted() {
		return currentPunchPlan != null;
	}

	private void logTextInformation(String message) {

		if (message.length() > 60) {
			message = message.substring(0, 57) + ".."; //$NON-NLS-1$
		}

		progress.setText(message);
		progress.repaint();
		// informationConsole.writeln(message);
		// informationConsole.repaint();
	}

	private void definePunchPlanning(PunchPlan punchPlanning) {
		this.currentPunchPlan = punchPlanning;
	}

	public String getDetails() {
		return Messages.getString("StepPlanning.18"); //$NON-NLS-1$
	}

	@Override
	public Icon getPageImage() {
		return null;
	}

	private void saveUserParameters(Class optimizerClass, Serializable savedParameters) {
		assert currentMachinePrefsStorage != null;
		ClassLoaderObjectsPrefsStorage ops = new ClassLoaderObjectsPrefsStorage(currentMachinePrefsStorage);
		logger.debug("save stored parameters"); //$NON-NLS-1$
		try {
			ops.saveObjectProperties(optimizerClass.getSimpleName(), savedParameters);
			rootPrefsStorage.save();
		} catch (Exception ex) {
			logger.debug("fail to save optimizer parameters :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	@Override
	public void dispose() {
		if (processingEngine != null) {
			processingEngine.dispose();
		}
	}

	/**
	 * check if issue collection has Hole too small
	 * 
	 * @param issueCollection
	 * @return
	 */
	private boolean hasTooSmallHoleErrors(IssueCollection issueCollection) {
		boolean toosmall = false;
		if (issueCollection != null) {
			for (AbstractIssue i : issueCollection) {
				if (i != null) {
					if (i instanceof IssueHole) {
						IssueHole ih = (IssueHole) i;
						if (ih.getType() == IssuesConstants.HOLE_TOO_SMALL) {
							toosmall = true;
							break;
						}
					}
				}
			}

		}
		return toosmall;
	}

}
