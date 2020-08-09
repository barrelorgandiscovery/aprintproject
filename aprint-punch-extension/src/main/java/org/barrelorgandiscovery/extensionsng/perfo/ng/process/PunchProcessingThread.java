package org.barrelorgandiscovery.extensionsng.perfo.ng.process;

import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.JobEvent;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.atrace.OptimizedObject;
import org.barrelorgandiscovery.gui.atrace.Optimizer;
import org.barrelorgandiscovery.gui.atrace.OptimizerProgress;
import org.barrelorgandiscovery.gui.atrace.OptimizerResult;
import org.barrelorgandiscovery.gui.atrace.ConverterResult;
import org.barrelorgandiscovery.gui.atrace.PunchLayer;
import org.barrelorgandiscovery.gui.issues.JIssuePresenter;
import org.barrelorgandiscovery.gui.script.groovy.ASyncConsoleOutput;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * this class process the punch from a virtual book, handle currently in
 * progress job, and associated GUI elements
 * 
 * @author pfreydiere
 * 
 */
public class PunchProcessingThread {

	private static Logger logger = LogManager
			.getLogger(PunchProcessingThread.class);

	private ASyncConsoleOutput consoleTextArea;

	private PunchLayer punchLayer;

	private JIssuePresenter issuePresenter;

	private JVirtualBookScrollableComponent jcarton;

	private CancelTracker currentTaskCancelTracker;

	public PunchProcessingThread(ASyncConsoleOutput consoleTextArea,
			PunchLayer punchLayer, JIssuePresenter issuePresenter) {

		this.consoleTextArea = consoleTextArea;

		this.punchLayer = punchLayer;

		this.issuePresenter = issuePresenter;
	}

	private PunchProcessingResult punchProcessingResultCallBack;

	public void setPunchProcessingResultCallBack(
			PunchProcessingResult punchProcessingResultCallBack) {
		this.punchProcessingResultCallBack = punchProcessingResultCallBack;
	}

	private AsyncJobsManager asyncJobsManager;

	public void setAsyncJobsManager(AsyncJobsManager asyncJobsManager) {
		this.asyncJobsManager = asyncJobsManager;
	}

	public void setJScrollableVirtualBookComponent(
			JVirtualBookScrollableComponent jcarton) {
		this.jcarton = jcarton;
	}

	/**
	 * compute the new punch plan
	 * 
	 * @param parameters
	 *            the optimizer parameters
	 * @param vb
	 *            the virtualbook
	 * @param cancelTracker
	 *            the track cancel
	 * @throws Exception
	 */
	public void execute(final Optimizer<OptimizedObject> optimizer, final VirtualBook vb,
			final ICancelTracker cancelTracker,
			final OptimizerProgress optimProgress) throws Exception {

		addConsoleLine("Start Converting to punch");

		if (asyncJobsManager == null) {
			throw new Exception("extension not properly initialized");
		}

		synchronized (this) {
			if (currentTaskCancelTracker != null) {
				currentTaskCancelTracker.cancel();
				currentTaskCancelTracker = null;
			}
		}

		final CancelTracker ct = new CancelTracker();
		currentTaskCancelTracker = ct;

		asyncJobsManager.submitAndExecuteJob(new Callable<Void>() {
			@Override
			public Void call() throws Exception {

				OptimizerProgress op = new OptimizerProgress() {

					@Override
					public void report(double progressIndicator,
							OptimizedObject[] orderedPunches, String message) {
						try {
							addConsoleLine(progressIndicator + " " + message);

							OptimizerResult<OptimizedObject> r = new OptimizerResult<OptimizedObject>();
							r.result = orderedPunches;

							signalNewResult(r);

							if (optimProgress != null)
								optimProgress.report(progressIndicator,
										orderedPunches, message);

						} catch (Exception ex) {
							logger.error("error :" + ex.getMessage(), ex); //$NON-NLS-1$
						}
					}
				};

				OptimizerResult<OptimizedObject> oresult = optimizer.optimize(vb, op, ct);

				if (ct.isCanceled()) // don't send result if cancelled
					return null;
				
				signalNewResult(oresult); // for display

				if (punchProcessingResultCallBack != null) {
					punchProcessingResultCallBack.result(oresult);
				}
				
				return null;
			}
		}, new JobEvent() {

			@Override
			public void jobFinished(Object result) {
				try {
					addConsoleLine("finished");
				} catch (Exception ex) {
					logger.error(ex);
				}
			}

			@Override
			public void jobError(Exception ex) {
				try {
					addConsoleLine("error in job :" + ex.getMessage());
					logger.error(ex);
				} catch (Exception _ex) {
					logger.error(_ex);
				}

			}

			@Override
			public void jobAborted() {

			}
		});

	}

	// ///////////////////////////////////////////////////////////////////
	// in gui thread

	private void addConsoleLine(final String message) throws Exception {

		if (consoleTextArea == null)
			return;

		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					consoleTextArea.appendOutput(message + "\n", null); //$NON-NLS-1$
				} catch (Exception ex) {
					logger.error(
							"fail to output to console :" + ex.getMessage(), ex); //$NON-NLS-1$
				}
			}
		};

		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeAndWait(r);
		}

	}


	/**
	 * called when a solution is given for the hole virtual book
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void signalNewResult(final ConverterResult<OptimizedObject> result)
			throws Exception {

		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				logger.debug("set punches"); //$NON-NLS-1$
				if (punchLayer != null) {
					punchLayer.setOptimizedObject(result.result);
				}
				if (issuePresenter != null)
					issuePresenter.loadIssues(result.holeerrors);

				if (jcarton != null) {
					jcarton.repaint();
				}
			}
		});
	}

}
