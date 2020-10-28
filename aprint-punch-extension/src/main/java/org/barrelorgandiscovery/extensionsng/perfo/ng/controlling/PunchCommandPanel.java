package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensionsng.console.JConsole;
import org.barrelorgandiscovery.extensionsng.perfo.ng.controlling.XYPanel.XYListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.controlling.wizard.PanelStep;
import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GCodeCompiler;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLLazerMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLLazerMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CutToCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.NearestCommandXYVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.XYCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.optimizers.OptimizersRepository;
import org.barrelorgandiscovery.gui.aedit.DistanceLayer;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.aedit.UndoStack;
import org.barrelorgandiscovery.gui.aedit.toolbar.JVBToolingToolbar;
import org.barrelorgandiscovery.gui.script.groovy.ASyncConsoleOutput;
import org.barrelorgandiscovery.gui.script.groovy.IScriptConsole;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepChanged;
import org.barrelorgandiscovery.gui.wizard.Wizard;
import org.barrelorgandiscovery.optimizers.Optimizer;
import org.barrelorgandiscovery.optimizers.OptimizerResult;
import org.barrelorgandiscovery.optimizers.cad.XOptim;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult;
import org.jdesktop.swingx.VerticalLayout;

import com.jeta.forms.components.border.TitledBorderLabel;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.GridView;

/**
 * Punch Panel
 * 
 * @author pfreydiere
 * 
 */
public class PunchCommandPanel extends JPanel implements Disposable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3591301386606927045L;

	private static Logger logger = Logger.getLogger(PunchCommandPanel.class);

	private PunchPlan punchPlan;

	private VirtualBook vb;

	private PunchController controller;

	private JToggleButton dontmovey;

	private FormPanel settingsPanel;

	private PositionPanel positionPanel;

	private PunchCommandLayer punchLayer;

	private JEditableVirtualBookComponent virtualBookComponent;

	private DistanceLayer distanceLayer;

	private JConsole console;

	// /////////////////////////////////////
	// offset for home delta

	private double xMachineOffset = 0.0;

	private double yShift = 0.0;

	private double yMachineOffset = 0.0;

	private double lastpositionMachineY = Double.NaN;

	// machine control
	private MachineControl machineControl;

	/**
	 * status label
	 */
	private JLabel statusLabel;

	/**
	 * user preferences storage
	 */
	private IPrefsStorage ps;

	public PunchCommandPanel(PunchPlan pp, VirtualBook vb, IPrefsStorage ps) throws Exception {

		assert ps != null;
		assert pp != null;
		assert vb != null;

		this.punchPlan = pp;
		this.vb = vb;
		this.ps = ps;

		initComponents();
	}

	private void checkMachineControl() throws Exception {
		if (machineControl == null)
			throw new Exception("no connected machine"); //$NON-NLS-1$
	}

	public void setMachineControl(final MachineControl mc) throws Exception {

		if (this.machineControl != null) {
			logger.debug("unregister events"); //$NON-NLS-1$
			
		}

		MachineControl withOffsetMachineControl = new MachineControl() {

			final MachineControl innerMc = mc;

			@Override
			public void setMachineControlListener(final MachineControlListener listener) {

				innerMc.setMachineControlListener(new MachineControlListener() {

					@Override
					public void statusChanged(MachineStatus status) {
						listener.statusChanged(status);
					}

					@Override
					public void error(String message) {
						listener.error(message);
					}

					@Override
					public void currentMachinePosition(String status, double mx, double my) {

						listener.currentMachinePosition(status, mx + xMachineOffset, my + yMachineOffset + yShift);
					}

					@Override
					public void rawElementSent(String commandSent) {
						listener.rawElementSent(commandSent);
					}

					@Override
					public void rawElementReceived(String commandReceived) {
						listener.rawElementReceived(commandReceived);
					}
					
					@Override
					public void informationReceived(String commands) {
						listener.informationReceived(commands);
					}

				});

			}

			@Override
			public void sendCommand(Command command) throws Exception {

				if (command instanceof XYCommand) {

					if (command instanceof DisplacementCommand) {
						DisplacementCommand pc = (DisplacementCommand) command;
						innerMc.sendCommand(new DisplacementCommand(pc.getX() + yMachineOffset + yShift,
								pc.getY() + xMachineOffset));

					} else if (command instanceof PunchCommand) {
						PunchCommand pc = (PunchCommand) command;
						innerMc.sendCommand(
								new PunchCommand(pc.getX() + yMachineOffset + yShift, pc.getY() + xMachineOffset));

					} else if (command instanceof CutToCommand) {
						CutToCommand cutToCommand = (CutToCommand) command;
						innerMc.sendCommand(new CutToCommand(cutToCommand.getX() + yMachineOffset + yShift,
								cutToCommand.getY() + xMachineOffset, cutToCommand.getPowerFactor(),
								cutToCommand.getSpeedFactor()));
					} else {
						throw new Exception("unsupported XYCommand :" + command); //$NON-NLS-1$
					}

				} else {
					innerMc.sendCommand(command);
				}

			}

			@Override
			public void flushCommands() throws Exception {
				innerMc.flushCommands();
			}

			@Override
			public void close() throws Exception {
				innerMc.close();
			}

			@Override
			public void reset() throws Exception {
				innerMc.reset();
			}

			@Override
			public void prepareForWork() throws Exception {
				innerMc.prepareForWork();
			}

			@Override
			public void endingForWork() throws Exception {
				innerMc.endingForWork();
			}

			@Override
			public MachineStatus getStatus() {
				return innerMc.getStatus();
			}

		};

		this.machineControl = withOffsetMachineControl;

		
		machineControl.setMachineControlListener(listenerGuiMachineControl);

		controller.setMachineControl(machineControl);

		defineCurrentCommandIndexAndUpdatePanel(0); // place on the origin

	}

	protected void initComponents() throws Exception {

		virtualBookComponent = new JEditableVirtualBookComponent();
		virtualBookComponent.setVirtualBook(vb);
		virtualBookComponent.setUseFastDrawing(true);
		setLayout(new BorderLayout());

		punchLayer = new PunchCommandLayer(virtualBookComponent);

		punchLayer.setPunchPlan(punchPlan);
		punchLayer.setVisible(true);
		punchLayer.setPunchHeight(3.0); // defaults
		punchLayer.setPunchWidth(3.0);
		punchLayer.setOrigin(PunchCommandLayer.ORIGIN_CENTER);

		virtualBookComponent.addLayer(punchLayer);

		distanceLayer = new DistanceLayer();
		virtualBookComponent.addLayer(distanceLayer);

		machinePositionLayer = new MachinePositionLayer();
		virtualBookComponent.addLayer(machinePositionLayer);

		virtualBookComponent.setMargin(30);

		JPanel pvb = new JPanel();
		pvb.setLayout(new BorderLayout());
		pvb.add(virtualBookComponent, BorderLayout.CENTER);

		// add toolbar
		JVBToolingToolbar tb = new JVBToolingToolbar(virtualBookComponent, new UndoStack(), null);

		pvb.add(tb, BorderLayout.NORTH);

		add(pvb, BorderLayout.CENTER);

		positionMachineTool = new Tool() {

			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					double x = virtualBookComponent.convertScreenXToCarton(e.getX());
					double y = virtualBookComponent.convertScreenYToCarton(e.getY());

					// search for command in the plan
					NearestCommandXYVisitor nv = new NearestCommandXYVisitor(x, y);
					nv.visit(punchPlan);
					XYCommand nearest = nv.getNearest();

					if (nearest != null) {
						clickOnPunchCommand(nearest);

					}

				} catch (Exception ex) {
					logger.error("error in handling click event :" + ex.getMessage(), //$NON-NLS-1$
							ex);
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				virtualBookComponent.setHightlight(virtualBookComponent.convertScreenXToCarton(e.getX()));
			}

			@Override
			public void activated() {
				try {
					virtualBookComponent.setCursor(CursorTools
							.createCursorWithImage(ImageTools.loadImage(getClass().getResource("smallpunch.png")))); //$NON-NLS-1$
				} catch (Exception ex) {
					logger.error("fail to load cursor image :" + ex.getMessage(), ex); //$NON-NLS-1$
				}
			}

			@Override
			public void unactivated() {
				virtualBookComponent.setCursor(Cursor.getDefaultCursor());
			}
		};

		JToggleButton t = tb.addTool(positionMachineTool);
		t.setToolTipText(Messages.getString("PunchCommandPanel.9")); //$NON-NLS-1$
		t.setIcon(new ImageIcon(getClass().getResource("smallpunch.png"))); //$NON-NLS-1$

		moveBookDuringPunch = new JToggleButton();
		moveBookDuringPunch.setIcon(ImageTools.loadIcon(this.getClass(), "view_punch.png")); //$NON-NLS-1$
		moveBookDuringPunch.setToolTipText(Messages.getString("PunchCommandPanel.12")); //$NON-NLS-1$
		tb.addSeparator();
		tb.add(moveBookDuringPunch);
		moveBookDuringPunch.setSelected(true);

		// by default, use the position machine tool
		virtualBookComponent.setCurrentTool(positionMachineTool);

		// //////////////////////////////////////////////////////////

		positionPanel = new PositionPanel();

		controller = new PunchController(punchLayer, positionPanel);
		positionPanel.setListener(controller);

		XYPanel xypanel = new XYPanel();
		xypanel.setOffsets(xMachineOffset, yMachineOffset);

		settingsPanel = new FormPanel(getClass().getResourceAsStream("settings.jfrm")); //$NON-NLS-1$

		TitledBorderLabel labelsettingposition = (TitledBorderLabel) settingsPanel.getComponentByName("lblposition"); //$NON-NLS-1$
		labelsettingposition.setText(Messages.getString("PunchCommandPanel.15")); //$NON-NLS-1$

		settingsPanel.getFormAccessor().replaceBean("xpanel", xypanel); //$NON-NLS-1$
		PanelStep settingsStep = new PanelStep(settingsPanel, "settings", //$NON-NLS-1$
				Messages.getString("PunchCommandPanel.18"), Messages.getString("PunchCommandPanel.19"), //$NON-NLS-1$ //$NON-NLS-2$
				new ImageIcon(getClass().getResource("misc.png"))); //$NON-NLS-1$

		PanelStep punchStep = new PanelStep(positionPanel, "punch", Messages.getString("PunchCommandPanel.22"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("PunchCommandPanel.23"), new ImageIcon(getClass() //$NON-NLS-1$
						.getResource("Perforer.png"))); //$NON-NLS-1$

		JLabel usepunchposition = settingsPanel.getLabel("lblusepunchposition"); //$NON-NLS-1$
		usepunchposition.setText(Messages.getString("PunchCommandPanel.123")); //$NON-NLS-1$

		punchStep.setParentStep(settingsStep);
		wizardPunch = new Wizard(Arrays.asList(new Step[] { settingsStep, punchStep }), null);

		wizardPunch.setBorder(new TitledBorder(Messages.getString("PunchCommandPanel.25"))); //$NON-NLS-1$

		wizardPunch.setShowLastButton(false);

		add(wizardPunch, BorderLayout.WEST);

		// /////////////////////////////////////////////////////////
		// status component

		JPanel pStatus = new JPanel();
		TitledBorder titleBorder = new TitledBorder("Status");
		pStatus.setBorder(titleBorder);
		pStatus.setLayout(new VerticalLayout());
		statusLabel = new JLabel();

		console = new JConsole();
		console.setPreferredSize(new Dimension(200, 100));

		pStatus.add(statusLabel);
		pStatus.add(console);

		add(pStatus, BorderLayout.SOUTH);
		updateStatus(Messages.getString("PunchCommandPanel.26"), Double.NaN, Double.NaN); //$NON-NLS-1$

		// //////////////////////////////////////////////////////////////////////////////
		// Virtual book events

		// //////////////////////////////////////////////////////////////////////////////
		// get principal components

		wizardPunch.setStepChanged(new StepChanged() {

			@Override
			public void currentStepChanged(int stepNo, Serializable state) {
				principalModeChanged();
			}
		});

		dontmovey = (JToggleButton) settingsPanel.getComponentByName("dontmovey"); //$NON-NLS-1$
		dontmovey.setSelected(true);

		// homing

		homingButton = (JButton) settingsPanel.getButton("homing"); //$NON-NLS-1$

		homingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				homingButtonCalled();
			}
		});
		homingButton.setIcon(new ImageIcon(getClass().getResource("gohome.png"))); //$NON-NLS-1$
		homingButton.setText(Messages.getString("PunchCommandPanel.30")); //$NON-NLS-1$
		homingButton.setToolTipText(Messages.getString("PunchCommandPanel.31")); //$NON-NLS-1$

		// reset
		resetButton = (JButton) settingsPanel.getButton("reset"); //$NON-NLS-1$
		resetButton.setText(Messages.getString("PunchCommandPanel.33")); //$NON-NLS-1$
		resetButton.setToolTipText(Messages.getString("PunchCommandPanel.34")); //$NON-NLS-1$
		resetButton.setIcon(new ImageIcon(getClass().getResource("connect_established.png"))); //$NON-NLS-1$
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					machineControl.reset();
				} catch (Exception ex) {
					logger.error("error resetting the machine :" + ex.getMessage(), ex); //$NON-NLS-1$
					JMessageBox.showError(this, ex);
				}
			}
		});

		// define defaults

		principalModeChanged();

		xypanel.setXYListener(new XYListener() {

			@Override
			public void xyChanged(double x, double y) {
				xMachineOffset = x;
				yMachineOffset = y;

				// launch a displacement for the machine
				// revert because the plan is in screen reference

				try {
					checkMachineControl();

					moveToCurrentPosition();

				} catch (Exception ex) {
					logger.error("error moving the punch :" + ex.getMessage(), //$NON-NLS-1$
							ex);
				}
			}
		});

		dontmovey.setSelected(true);
		dontmovey.setText(Messages.getString("PunchCommandPanel.38")); //$NON-NLS-1$
		dontmovey.setToolTipText(Messages.getString("PunchCommandPanel.39")); //$NON-NLS-1$
		// dontmovey
		dontmovey.setIcon(new ImageIcon(getClass().getResource("bothmove.png"))); //$NON-NLS-1$

		GridView actions = (GridView) settingsPanel.getFormAccessor("actionbuttons"); //$NON-NLS-1$
		JButton punch = (JButton) actions.getComponentByName("punch"); //$NON-NLS-1$
		punch.setText(Messages.getString("PunchCommandPanel.43")); //$NON-NLS-1$
		punch.setIcon(new ImageIcon(getClass().getResource("smallpunch.png"))); //$NON-NLS-1$
		punch.setToolTipText(Messages.getString("PunchCommandPanel.45")); //$NON-NLS-1$

		punch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					logger.debug("punch"); //$NON-NLS-1$

					XYCommand xyc = getCurrentSelectedXYCommand();

					PunchCommand punchCommand = new PunchCommand(xyc.getX(), xyc.getY());
					machineControl.sendCommand(punchCommand);

					logger.debug("Done !"); //$NON-NLS-1$

				} catch (Exception ex) {
					logger.error("error while punching :" + ex.getMessage(), ex); //$NON-NLS-1$
				}

			}
		});

		JTextField mmnumber = (JTextField) actions.getComponentByName("mmsize"); //$NON-NLS-1$

		double defaultshift = ps.getDoubleProperty("shiftproperty", 160.0); //$NON-NLS-1$
		mmnumber.setText(Double.toString(defaultshift));

		JButton moveForward = (JButton) actions.getComponentByName("forward"); //$NON-NLS-1$

		moveForward.setText(Messages.getString("PunchCommandPanel.50")); //$NON-NLS-1$
		moveForward.setIcon(new ImageIcon(getClass().getResource("fastforward.png"))); //$NON-NLS-1$
		moveForward.setToolTipText(Messages.getString("PunchCommandPanel.52")); //$NON-NLS-1$
		moveForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					logger.debug("move forward"); //$NON-NLS-1$

					String textmm = mmnumber.getText();
					double shift = Double.parseDouble(textmm);
					ps.setDoubleProperty("shiftproperty", shift); //$NON-NLS-1$
					ps.save();

					yShift += shift; // 16 cm move
					moveToCurrentPosition();

				} catch (Exception ex) {
					logger.error("error while moving forward :" + ex.getMessage(), //$NON-NLS-1$
							ex);
				}

			}
		});

	}

	/**
	 * know if we are in setting mode
	 * 
	 * @return
	 */
	private boolean isSettingMode() {
		return wizardPunch.getCurrentStepIndex() == 0;
	}

	private boolean mustMachineMoveOnYInSettings() {
		return !dontmovey.isSelected();
	}

	// ///////////////////////////////////////////////////////////////////////

	/**
	 * manage the principal mode change
	 */
	private void principalModeChanged() {
		logger.debug("is settings activated :" + isSettingMode()); //$NON-NLS-1$
		SwingUtils.recurseSetEnable(settingsPanel, isSettingMode() && !controller.isRunning());
		SwingUtils.recurseSetEnable(positionPanel, !isSettingMode());
	}

	private void clickOnPunchCommand(XYCommand xycommand) {

		if (xycommand == null)
			return;

		int index = punchPlan.getCommandsByRef().indexOf(xycommand);

		if (index != -1) {
			if (isSettingMode()) {

				defineCurrentCommandIndexAndUpdatePanel(index);
				virtualBookComponent.repaint();

				try {

					if (dontmovey.isSelected()) {
						// get the current y, to not move the punch
						yShift = -xycommand.getX() + lastpositionMachineY;
					} else {
						// yshift = 0;
					}

					DisplacementCommand pos = new DisplacementCommand(xycommand.getX(), xycommand.getY());

					checkMachineControl();

					machineControl.sendCommand(pos);

				} catch (Exception ex) {
					logger.error("error in machine displacement :" + ex.getMessage(), //$NON-NLS-1$
							ex);
				}

			} else {
				// punch mode

				if (controller.isRunning()) {
					// currently streaming the commands,
					// no action associated, user must stop the process
				} else {

					// get the current command
					// and position it to the selected position

					try {

						defineCurrentCommandIndexAndUpdatePanel(index);

						virtualBookComponent.repaint();

						DisplacementCommand pos = new DisplacementCommand(xycommand.getX(), xycommand.getY());

						checkMachineControl();

						machineControl.sendCommand(pos);
						// machineControl.flushCommands();
					} catch (Exception ex) {
						logger.error("error in machine displacement :" //$NON-NLS-1$
								+ ex.getMessage(), ex);
					}
				}
			}

		}

	}

	private void defineCurrentCommandIndexAndUpdatePanel(int index) {
		punchLayer.setCurrentPos(index);
		int nbCommandsToSend = punchPlan.getCommandsByRef().size();

		positionPanel.updateState(index, nbCommandsToSend, index > 0, index < nbCommandsToSend - 1, "--", "--", "--", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"--");
	}

	// ///////////////////////////////////////////////////////////////
	//

	private void updateStatus(String statusMessage, double posx, double posy) {
		String status = Messages.getString("PunchCommandPanel.61") + statusMessage + " Position :" + posx + "," + posy; //$NON-NLS-1$
		statusLabel.setText(status);
	}

	// //////////////////////////////////////////////////////////////
	// punch phase

	private MachinePositionLayer machinePositionLayer;

	private JButton homingButton;

	private JButton resetButton;

	// ////////////////////////////////////////////////////////
	// state for the punch process

	private Wizard wizardPunch;

	private Tool positionMachineTool;

	private JToggleButton moveBookDuringPunch;

	private MachineControlListener listenerGuiMachineControl = new MachineControlListener() {

		@Override
		public void statusChanged(MachineStatus status) {

		}

		@Override
		public void error(String message) {

			SwingUtilities.invokeLater(() -> {
				try {

					if (!"?".equals(message)) {
						console.writeln(message);
						console.repaint();
					}
				} catch (Exception ex) {

				}
			});

		}

		@Override
		public void rawElementSent(String commandSent) {

			SwingUtilities.invokeLater(() -> {
				try {

					if (!"?".equals(commandSent)) {
						console.write(commandSent);
						console.repaint();
					}
				} catch (Exception ex) {

				}
			});
		}

		@Override
		public void rawElementReceived(String commandReceived) {
			
		}
		
		@Override
		public void informationReceived(String commands) {
			SwingUtilities.invokeLater(() -> {
				try {
					if (!"?".equals(commands)) {
						console.write(commands);
						console.repaint();
					}
				} catch (Exception ex) {

				}
			});
		}
		
		

		long lastDisplayedFeedBack = System.currentTimeMillis();

		@Override
		public void currentMachinePosition(final String status, final double mx, final double my) {
			try {

				if ((System.currentTimeMillis() - lastDisplayedFeedBack) > 500) {

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							// "Machine Position X:" + mx + " Y:"
							// + my + " ,
							updateStatus(status, mx // $NON-NLS-1$
							, my); // $NON-NLS-1$
							lastpositionMachineY = my;

							machinePositionLayer.setMachinePosition(mx - xMachineOffset,
									my - yMachineOffset - yShift);

							// if machine position is outside the current
							// view
							// move the view to see it
							if (moveBookDuringPunch.isSelected()) {

								boolean isIn = true;
								double machinex = machinePositionLayer.getY();

								double xmax = virtualBookComponent
										.convertScreenXToCarton(virtualBookComponent.getWidth());

								if (machinex > xmax)
									isIn = false;

								if (machinex < virtualBookComponent.convertScreenXToCarton(0))
									isIn = false;

								double widthInMM = virtualBookComponent.pixelToMM(virtualBookComponent.getWidth());

								if (!isIn) {
									if (controller.isRunning()) {
										// align the punch on left, 1/5
										virtualBookComponent.setXoffset(machinex - 1.0 / 5 * widthInMM);

									}
								}

							}

							machinePositionLayer.setStatus(status);
							virtualBookComponent.repaint();
						}
					});
					lastDisplayedFeedBack = System.currentTimeMillis();
				}

			} catch (Exception ex) {
				logger.error("error in feedback :" + ex.getMessage(), ex); //$NON-NLS-1$
			}
		}

		
	};

	/**
	 * user has pushed the homing button
	 */
	private void homingButtonCalled() {
		try {

			checkMachineControl();
			HomingCommand hc = new HomingCommand();
			machineControl.sendCommand(hc);

		} catch (Exception ex) {
			logger.error("error calling homing button :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	/**
	 * get current XYCommand, return null if the current command is not an xy
	 * command
	 * 
	 * @return
	 * @throws Exception
	 */
	private XYCommand getCurrentSelectedXYCommand() throws Exception {
		// get selected position
		Integer currentSelectedIndex = punchLayer.getCurrentPos();
		Command command = punchPlan.getCommandsByRef().get(currentSelectedIndex);

		if (command instanceof XYCommand) {
			XYCommand xy = (XYCommand) command;
			return xy;
		}
		return null;
	}

	/**
	 * move the punch to current selected position
	 * 
	 * @throws Exception
	 */
	private void moveToCurrentPosition() throws Exception {
		// get selected position

		XYCommand currentSelectedXYCommand = getCurrentSelectedXYCommand();
		if (currentSelectedXYCommand != null) {
			double px = currentSelectedXYCommand.getX();
			double py = currentSelectedXYCommand.getY();
			machineControl.sendCommand(new DisplacementCommand(px, py));
			return;
		}
		throw new Exception("implementation exception"); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		try {
			if (machineControl != null) {
				machineControl.close();
			}

			if (controller != null) {
				controller.dispose();
			}

		} catch (Exception ex) {
			logger.error("dispose error :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	/**
	 * test method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		JFrame f = new JFrame();
		f.setSize(600, 400);

		f.setLayout(new BorderLayout());

		VirtualBookResult r = VirtualBookXmlIO
				.read(new File("C:\\Users\\use\\Dropbox\\APrint\\Books\\27-29\\Complainte de la butte.book")); //$NON-NLS-1$

		// convert to punchplan
//
//		PunchConverter pc = new PunchConverter(r.virtualBook.getScale(), 3.0);
//		OptimizerResult<Punch> punches = pc.convert(r.virtualBook.getHolesCopy());
//
//		PunchPlan pp = PunchDefaultConverter.createDefaultPunchPlan(punches.result);
//		pp.getCommandsByRef().add(0, new DisplacementCommand(0, 0));

//		MockMachineParameters mockMachineParameters = new MockMachineParameters();
//		AbstractMachine machine = mockMachineParameters.createAssociatedMachineInstance();
//		MachineControl machineControl = machine.open(mockMachineParameters);
//		

//		GRBLPunchMachineParameters grblMachineParameters = new GRBLPunchMachineParameters();
//		grblMachineParameters.setComPort("COM4");
//
//		GRBLPunchMachine grblMachine = new GRBLPunchMachine();
//		MachineControl machineControl = grblMachine.open(grblMachineParameters);

		
		
		GRBLLazerMachineParameters grblMachineParameters = new GRBLLazerMachineParameters();
		grblMachineParameters.setComPort("COM4");

		GRBLLazerMachine grblMachine = new GRBLLazerMachine();
		OptimizersRepository optRepository = new OptimizersRepository();
		Optimizer opt = optRepository
				.newOptimizerWithParameters(optRepository.instanciateParametersForOptimizer(XOptim.class));
		OptimizerResult optimize = opt.optimize(r.virtualBook);
		PunchPlan pp = optRepository.createDefaultPunchPlanForLazerMachine(grblMachineParameters, optimize.result);

		MachineControl machineControl = grblMachine.open(grblMachineParameters);

		IPrefsStorage dps = new FilePrefsStorage(new File("c:\\temp\\prefsperfo"));
		dps.load();

		PunchCommandPanel p = new PunchCommandPanel(pp, r.virtualBook, dps);

		p.setMachineControl(machineControl);

		f.add(p, BorderLayout.CENTER);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}

}
