package org.barrelorgandiscovery.gui.wizard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.BareBonesBrowserLaunch;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.JMessageBox;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * Wizard component, maintain the wizard gui, maintain the current step state,
 * maintain the navigation logic
 *
 * @author pfreydiere
 */
public class Wizard extends JPanel implements Disposable {

	/** */
	private static final long serialVersionUID = 5476366764026686586L;

	/** logger */
	private static Logger logger = Logger.getLogger(Wizard.class);

	/** step list */
	private List<Step> steps;

	/** id of the current step */
	private int currentStepNo;

	/** finished listener */
	private FinishedListener finishedListener;

	/** wizard GUI */
	private FormPanel formPanel;

	/** nextButton */
	private JButton next;

	/** text label */
	private String overridenLastStepLabel = null;

	/** previous button */
	private JButton previous;

	/** Label of the step */
	private JLabel labelStep;

	/** Details of the step */
	private JLabel labelDetails;

	/** remember the steps states */
	private WizardStates stepsContents = new WizardStates();

	/** listener for the status changed */
	private StepStatusChangedListener stepStatusChangedListener;

	/** changed step */
	private StepChanged stepChanged;

	/** before step changed */
	private StepBeforeChanged stepBeforeChanged;

	/** help url */
	private String helpURL;

	/**
	 * Construct the wizard, with all the steps
	 *
	 * @param steps
	 */
	public Wizard(List<Step> steps, Serializable initialState) throws Exception {

		assert steps != null;
		assert steps.size() > 0;
		this.steps = new ArrayList<>(steps); // copy list

		currentStepNo = 0;

		try {
			stepsContents = (WizardStates) initialState;

		} catch (Exception ex) {
			logger.error("error reading the state, back to defaults"); //$NON-NLS-1$
		}

		if (stepsContents == null)
			stepsContents = new WizardStates();

		initComponent();

		stepStatusChangedListener = new StepStatusChangedListener() {
			public void stepStatusChanged() {
				refreshButtons();
			}
		};

		moveToStep(0, false);
	}

	public void changeFurtherStepList(List<Step> nextsSteps) {

		logger.debug("current step :" + currentStepNo);
		if (currentStepNo < steps.size() - 1) {
			List<Step> toremove = steps.subList(currentStepNo + 1, steps.size());
			steps.removeAll(toremove);
		}

		if (nextsSteps != null) {
			steps.addAll(nextsSteps);
		}

		refreshButtons();
	}

	/**
	 * define the last step label name
	 *
	 * @param lastStepLabel
	 */
	public void defineLastStepLabelName(String lastStepLabel) {
		this.overridenLastStepLabel = lastStepLabel;
		refreshButtons();
	}

	/**
	 * flag for display the last operation in wizard : finish
	 */
	private boolean showLastTerminateButton = true;

	public void setShowLastButton(boolean showLastButton) {
		this.showLastTerminateButton = showLastButton;
		refreshButtons();
	}

	/**
	 * init the wizard using the initial state in parameters
	 * 
	 * @param initialState
	 * @throws Exception
	 */
	public void reinit(Serializable initialState) throws Exception {
		try {
			stepsContents = (WizardStates) initialState;

		} catch (Exception ex) {
			logger.error("error reading the state, back to defaults"); //$NON-NLS-1$
			this.stepsContents = new WizardStates();
		}

		if (initialState == null)
			this.stepsContents = new WizardStates();

		toFirst();
	}

	/**
	 * try to load steps
	 *
	 * @param initialState
	 * @throws Exception
	 */
	public void reloadStatesIfPossible(Serializable initialState, Step[] stepList) throws Exception {

		WizardStates loadedStepsStates = null;
		try {
			loadedStepsStates = (WizardStates) initialState;

			if (stepList != null) {
				for (Step step : stepList) {
					assert stepsContents != null;
					stepsContents.setState(step, loadedStepsStates.getState(step));
					logger.debug("step state loaded :" + step);
				}
			}

		} catch (Exception ex) {
			logger.info("states cannot be loaded");
		}
	}

	/**
	 * define the help url
	 * 
	 * @param helpurl
	 */
	public void setHelpUrl(String helpurl) {
		this.helpURL = helpurl;
	}

	/**
	 * init the internal components
	 *
	 * @throws Exception
	 */
	private void initComponent() throws Exception {

		formPanel = new FormPanel(getClass().getResourceAsStream("wizard.jfrm")); //$NON-NLS-1$
		setLayout(new BorderLayout());
		add(formPanel, BorderLayout.CENTER);

		next = (JButton) formPanel.getButton("btnnext"); //$NON-NLS-1$
		previous = (JButton) formPanel.getButton("btnprevious"); //$NON-NLS-1$
		labelStep = (JLabel) formPanel.getLabel("labelstep"); //$NON-NLS-1$

		JLabel helplabel = formPanel.getLabel("helplabel"); //$NON-NLS-1$
		helplabel.setIcon(new ImageIcon(getClass().getResource("help.png"))); //$NON-NLS-1$

		helplabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				String h = helpURL;

				if (h == null) {
					// default value
					h = "http://barrel-organ-discovery.org/site"; //$NON-NLS-1$
				}

				BareBonesBrowserLaunch.openURL(h);
			}
		});
		helplabel.setToolTipText(Messages.getString("Wizard.5")); //$NON-NLS-1$
		helplabel.setText(""); //$NON-NLS-1$

		labelDetails = (JLabel) formPanel.getLabel("explain"); //$NON-NLS-1$

		previous.setText(Messages.getString("Wizard.6")); //$NON-NLS-1$

		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					if (isTerminated()) {
						logger.debug("finished"); //$NON-NLS-1$
						finished();
					} else {
						assert canGoNext();

						moveToStep(currentStepNo + 1, true);
					}
				} catch (Exception ex) {
					logger.error("error while going next :" + ex.getMessage(), //$NON-NLS-1$
							ex);
					JMessageBox.showError(Wizard.this, ex);
				}
			}
		});

		previous.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					moveToStep(currentStepNo - 1, true);
				} catch (Exception ex) {
					logger.error("error while going next :" + ex.getMessage(), //$NON-NLS-1$
							ex);
				}
			}
		});

		refreshButtons();
	}

	private JComponent currentWizardComponent = null;

	/**
	 * Change the current wizard panel
	 *
	 * @param comp the component
	 */
	private void changeWizardPanel(JComponent comp) {

		assert comp != null;

		FormAccessor fa = formPanel.getFormAccessor();

		if (currentWizardComponent != null) {
			// already changed component
			fa.replaceBean(currentWizardComponent, comp);
		} else {
			// first component placing
			fa.replaceBean("component", comp); //$NON-NLS-1$
		}

		// do the layout to compute internally the width and height
		formPanel.validate();

		// the current wizard component
		currentWizardComponent = comp;
	}

	/** move to the next step */
	private void moveToStep(int stepNo, boolean saveCurrentState) throws Exception {

		assert stepsContents != null;

		logger.debug("move to step :" + stepNo); //$NON-NLS-1$

		// ensure we can move to the next step
		if (saveCurrentState) {
			logger.debug("save state for stepNo " + stepNo); //$NON-NLS-1$
			// assert getCurrentStep().isStepCompleted(); // the curent is
			// probably not completed when the user strick "previous"
			assert stepNo >= 0 && stepNo < steps.size();

			// unactivate and save the state
			Step curStep = getCurrentStep();
			assert curStep != null;
			logger.debug("unactivate " + curStep);
			Serializable oldcurrentStepState = curStep.unActivateAndGetSavedState();
			logger.debug("state saved associated to the set :" + oldcurrentStepState); //$NON-NLS-1$
			stepsContents.setState(curStep, oldcurrentStepState);
		}

		// get the next step
		Step nextstep = steps.get(stepNo);

		if (stepBeforeChanged != null) {
			boolean result = true;
			try {
				result = stepBeforeChanged.beforeStepChanged(getCurrentStep(), nextstep, this);
			} catch (Exception ex) {
				logger.error("error while asking for step changed :" + ex.getMessage(), ex);
			}
			if (!result) {
				logger.info(
						"can't move from " + getCurrentStepIndex() + " to " + nextstep + " beforechanged return false");
				return;
			}
		}

		assert nextstep instanceof JComponent;

		changeWizardPanel((JComponent) nextstep);
		currentStepNo = stepNo;

		String stepDetails = nextstep.getDetails();
		if (stepDetails == null)
			stepDetails = ""; //$NON-NLS-1$
		labelDetails.setText(stepDetails);

		// activate the new step
		logger.debug("activate step " + nextstep); //$NON-NLS-1$

		Serializable currentStepState = stepsContents.getState(getCurrentStep());

		logger.debug("activate :" + nextstep); //$NON-NLS-1$
		
	
		nextstep.activate(currentStepState, stepsContents, stepStatusChangedListener);
		
		// adjust the buttons states

		refreshButtons();

		if (stepChanged != null) {
			logger.debug("call step changed"); //$NON-NLS-1$
			stepChanged.currentStepChanged(stepNo, currentStepState);
		}

		// change Icon

	}

	/** refresh the next and previous button state */
	private void refreshButtons() {
		boolean nextState = false;
		boolean previousState = false;

		if (canGoNext()) {
			nextState = true;
		}

		if (currentStepNo - 1 >= 0) {
			previousState = true;
		}

		if (currentStepNo >= steps.size() - 1) {
			// last step

			next.setVisible(showLastTerminateButton);

			if (overridenLastStepLabel != null) {
				next.setText(overridenLastStepLabel);
			} else {
				next.setText(Messages.getString("Wizard.17")); //$NON-NLS-1$
			}

			next.setIcon(new ImageIcon(getClass().getResource("wizard.png"))); //$NON-NLS-1$
			if (getCurrentStep().isStepCompleted()) {
				nextState = true;
			}
			next.setToolTipText(Messages.getString("Wizard.1")); //$NON-NLS-1$

		} else {
			next.setText(Messages.getString("Wizard.19")); //$NON-NLS-1$
			next.setIcon(new ImageIcon(getClass().getResource("forward.png"))); //$NON-NLS-1$
			next.setToolTipText(Messages.getString("Wizard.2")); //$NON-NLS-1$
			next.setVisible(true);
		}

		next.setEnabled(nextState);

		previous.setEnabled(previousState);
		if (previousState) {
			previous.setToolTipText(Messages.getString("Wizard.4")); //$NON-NLS-1$
		} else {
			previous.setToolTipText(Messages.getString("Wizard.3")); //$NON-NLS-1$
		}

		previous.setIcon(new ImageIcon(getClass().getResource("back.png"))); //$NON-NLS-1$

		labelStep.setText(getCurrentStep().getLabel());

		Icon pageImage = getCurrentStep().getPageImage();
		if (pageImage == null) {
			pageImage = new ImageIcon(getClass().getResource("help_index.png"));
		}

		labelStep.setIcon(pageImage); // $NON-NLS-1$
	}

	/**
	 * define the finished listener
	 *
	 * @param finishedListener
	 */
	public void setFinishedListener(FinishedListener finishedListener) {
		this.finishedListener = finishedListener;
	}

	/**
	 * Go on the first step, and activate the step
	 *
	 * @throws Exception
	 */
	public void toFirst() throws Exception {
		assert steps.size() > 0;
		moveToStep(0, false);
	}

	/** @return */
	public Serializable getCurrentWizardState() {
		return stepsContents.getState(getCurrentStep());
	}

	/**
	 * count all steps, return the number of steps in the collection
	 *
	 * @return
	 */
	protected int countAllSteps() {
		return steps.size();
	}

	/**
	 * signal we can move to the next step in wizard
	 *
	 * @return
	 */
	public boolean canGoNext() {
		return currentStepNo < steps.size() - 1 && steps.get(currentStepNo).isStepCompleted();
	}

	/** @return */
	public boolean isTerminated() {
		return currentStepNo == (steps.size() - 1) && getCurrentStep().isStepCompleted();
	}

	/**
	 * get the current activated step
	 *
	 * @return
	 */
	private Step getCurrentStep() {
		assert currentStepNo < steps.size();
		return steps.get(currentStepNo);
	}

	/**
	 * return the current step index
	 *
	 * @return
	 */
	public int getCurrentStepIndex() {
		return currentStepNo;
	}

	/** move next, if possible */
	public void moveNext() throws Exception {
		if (canGoNext()) {
			moveToStep(currentStepNo + 1, true);
		}
	}

	/**
	 * called when the finished button is called
	 *
	 * @throws Exception
	 */
	private void finished() throws Exception {

		assert stepsContents != null;
		Serializable savedStep = getCurrentStep().unActivateAndGetSavedState();
		stepsContents.setState(getCurrentStep(), savedStep);

		logger.debug("launch the finished listener"); //$NON-NLS-1$
		if (finishedListener == null) {
			logger.warn("finishedListener is null, cannot call"); //$NON-NLS-1$
		} else {
			finishedListener.finished(stepsContents);
		}

		if (stepChanged != null) {
			logger.debug("call step changed"); //$NON-NLS-1$
			stepChanged.currentStepChanged(currentStepNo, savedStep);
		}
	}

	/**
	 * get the state of the pages and user input
	 *
	 * @return
	 */
	public Serializable getPagesStates() {
		return this.stepsContents;
	}

	/**
	 * define the listener
	 *
	 * @param stepChanged
	 */
	public void setStepChanged(StepChanged stepChanged) {
		this.stepChanged = stepChanged;
	}

	/**
	 * define the listener of before step changed
	 *
	 * @param stepBeforeChanged
	 */
	public void setStepBeforeChanged(StepBeforeChanged stepBeforeChanged) {
		this.stepBeforeChanged = stepBeforeChanged;
	}

	@Override
	public void dispose() {

		logger.debug("dispose wizard");
		if (steps != null) {

			for (Step s : steps) {
				logger.debug("dispose step " + s);
				try {
					if (s instanceof Disposable) {
						((Disposable) s).dispose();
					}
				} catch (Exception ex) {
					logger.debug("error in disposing step :" + s, ex);
				}
			}
		}
	}
}
