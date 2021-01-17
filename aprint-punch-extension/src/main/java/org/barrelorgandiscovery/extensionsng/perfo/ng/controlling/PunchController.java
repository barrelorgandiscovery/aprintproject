package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineCommandStream;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineCommandStream.StreamingProcessingListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.PauseTimerState;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.StatisticVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.XYCommand;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.JMessageBox;

/**
 * manage the punch process state
 * 
 * @author pfreydiere
 * 
 */
public class PunchController implements PositionPanelListener, Disposable {

	private static Logger logger = Logger.getLogger(PunchController.class);

	private PunchCommandLayer pl;

	private JPositionPanel posPanel;

	private MachineControl machineControl;

	private MachineCommandStream machineCommandStream;

	private PunchStatisticCollector punchStatisticCollector = new PunchStatisticCollector(19);
	
	private PauseTimerGetter pauseTimerGetter;

	private String timeLeft = "--:--:--"; //$NON-NLS-1$
	private String distanceLeft = "----- m"; //$NON-NLS-1$
	private String bookDistanceLeft = "----- m"; //$NON-NLS-1$
	private String bookMetersDone = "----- m"; //$NON-NLS-1$

	private long nboperationsleft = 0;

	/**
	 * display feedback
	 */
	private ExecutorService displayFeedBackExec = Executors.newSingleThreadExecutor();

	@Override
	public void dispose() {
		if (displayFeedBackExec != null) {
			displayFeedBackExec.shutdown();
		}
	}

	public PunchController(PunchCommandLayer pcl, JPositionPanel posPanel) {

		this.pl = pcl;
		Integer currentPos = pl.getCurrentPos();
		if (currentPos == null)
			currentPos = 0;
		pl.setCurrentPos(currentPos);

		this.posPanel = posPanel;

	}

	public void setMachineControl(MachineControl machineControl) {
		assert machineControl != null;
		this.machineControl = machineControl;
	}
	
	public void setPauseTimerGetter(PauseTimerGetter pauseTimerGetter) {
		this.pauseTimerGetter = pauseTimerGetter;
	}
	
	

	/**
	 * 
	 */
	private void updatePosPanel() {

		Integer currentPos = pl.getCurrentPos();

		PunchPlan punchPlan = pl.getPunchPlan();
		int allcmds = punchPlan.getCommandsByRef().size();

		posPanel.updateState(currentPos, allcmds, currentPos > 0 && !isRunning(), currentPos < allcmds && !isRunning(),
				timeLeft, distanceLeft, bookDistanceLeft, bookMetersDone);

		posPanel.setPlayState(isRunning());

	}

	public boolean isRunning() {
		return machineCommandStream != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.controlling.
	 * PositionPanelListener#next()
	 */
	@Override
	public void next() {
		Integer currentPos = pl.getCurrentPos();
		currentPos++;
		defineNewPosAndUpdatePanel(currentPos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.controlling.
	 * PositionPanelListener#previous()
	 */
	@Override
	public void previous() {
		Integer currentPos = pl.getCurrentPos();
		currentPos--;
		defineNewPosAndUpdatePanel(currentPos);
	}

	/**
	 * update panel to display current position
	 * 
	 * @param currentPos
	 */
	public void defineNewPosAndUpdatePanel(int currentPos) {
		pl.setCurrentPos(currentPos);
		updatePosPanel();
	}

	@Override
	public void first() {
		Integer currentPos = pl.getCurrentPos();
		currentPos = 0;
		defineNewPosAndUpdatePanel(currentPos);
	}

	/**
	 * format duration
	 * 
	 * @param duration
	 * @return
	 */
	public static String formatDuration(Duration duration) {
		long seconds = duration.getSeconds();
		long absSeconds = Math.abs(seconds);
		String positive = String.format("%d:%02d:%02d", absSeconds / 3600, //$NON-NLS-1$
				(absSeconds % 3600) / 60, absSeconds % 60);
		return seconds < 0 ? "-" + positive : positive; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.extensionsng.perfo.ng.controlling.
	 * PositionPanelListener#pausePlay()
	 */
	@Override
	public void pausePlay() throws Exception {

		if (machineCommandStream != null) {
			// stop
			machineCommandStream.stopStreaming();
			machineCommandStream = null;
			updatePosPanel();

		} else {
			// run , get the index

			Integer pos = pl.getCurrentPos();

			StatisticVisitor sv = new StatisticVisitor(false);
			sv.visit(pl.getPunchPlan(), pos);

			final double distanceLeft = sv.getDistanceDeplacement();

			machineCommandStream = new MachineCommandStream(machineControl, pl.getPunchPlan(),
					new StreamingProcessingListener() {

						@Override
						public void commandProcessed(final int index) {

							double distance = 0.0;
							PunchPlan punchPlan = pl.getPunchPlan();
							assert punchPlan != null;

							if (index > 0) {
								// we can compute the distance

								XYCommand p1 = punchPlan.getXYCommandAtIndex(index - 1);
								XYCommand p2 = punchPlan.getXYCommandAtIndex(index);

								if (p1 != null && p2 != null) {
									distance = Math.sqrt(
											Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
								}

								XYCommand latestXYCommand = punchPlan.getLatestXYCommand();
								XYCommand firstXYCommand = punchPlan.getFirstXYCommand();

								if (p2 != null) {
									double total = Double.NaN;
									if (firstXYCommand != null) {
										total = latestXYCommand.getX() - firstXYCommand.getX();
									}
									bookDistanceLeft = String.format("%1$6.3f m / %2$6.3f m", //$NON-NLS-1$
											(latestXYCommand.getX() - p2.getX()) / 1000.0, total / 1000.0);
									bookMetersDone = String.format("%1$6.3f m", //$NON-NLS-1$
											p2.getX() / 1000.0);

								}

							}

							punchStatisticCollector.informPunchStopped(distance);

							punchStatisticCollector.informStartPunch();

							// update time left every
							// 10 pos, from latests positions

							// compute the distance traveled

							nboperationsleft = pl.getPunchPlan().getCommandsByRef().size() - index;

							if (index % 10 == 0) {
								// every 10 position, update
								timeLeft = formatDuration(Duration.ofMillis((long) (nboperationsleft
										* punchStatisticCollector.getMeanTimePerPunch() * 1000)));
							}

							PunchController.this.distanceLeft = String.format("%1$6.3f m", //$NON-NLS-1$
									punchStatisticCollector.getDistanceLeft() / 1000.0);

							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									defineNewPosAndUpdatePanel(index);
								}
							});

							// if status is at alarm,
							// warn the user

						}

						@Override
						public void allCommandsEnded() {
							try {
								logger.info("all commands processed");
								// end of the processing ...
								machineCommandStream = null;
								updatePosPanel();
								logger.info("end of punch processing"); //$NON-NLS-1$
							} catch (Exception ex) {
								logger.error("error while stopping the stream ," //$NON-NLS-1$
										+ ex.getMessage(), ex);
							}
						}

						@Override
						public void errorInProcessing(Exception ex) {

							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									JMessageBox.showError(null, ex);
								}
							});

						}

						@Override
						public void currentStreamState(int currentStreamState) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									switch (currentStreamState) {
									case MachineCommandStream.STATE_PROCESSING:
										posPanel.updateStatus("PROCESSING", null);
										break;

									case MachineCommandStream.STATE_PAUSED:
										posPanel.updateStatus("PAUSED", null);
										break;

									default:
										posPanel.updateStatus("-", null);
									}
								}
							});
						}

					}, /* pause timer */ null);

			punchStatisticCollector.resetDistance(distanceLeft);
			// start
			punchStatisticCollector.informStartPunch();
			
			if (pauseTimerGetter != null) {
				PauseTimerState timerState = pauseTimerGetter.getNullOrConstructedTimer();
				machineCommandStream.setPauseTimerState(timerState);
			}
			
			machineCommandStream.startStreamFrom(pos);
			updatePosPanel();

		}

	}
}
