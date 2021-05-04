package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.extensionsng.perfo.gui.PunchLayer;
import org.barrelorgandiscovery.extensionsng.perfo.ng.optimizers.OptimizersRepository;
import org.barrelorgandiscovery.extensionsng.perfo.ng.process.PunchProcessingResult;
import org.barrelorgandiscovery.extensionsng.perfo.ng.process.PunchProcessingThread;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.issues.JIssuePresenter;
import org.barrelorgandiscovery.gui.script.groovy.ASyncConsoleOutput;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.issues.IssueLayer;
import org.barrelorgandiscovery.optimizers.Optimizer;
import org.barrelorgandiscovery.optimizers.OptimizerProgress;
import org.barrelorgandiscovery.optimizers.OptimizerResult;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;
import org.barrelorgandiscovery.optimizers.punch.PunchConverterOptimizerParameters;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class ProcessingOptimizerEngine implements Disposable {

	private static Logger logger = Logger
			.getLogger(ProcessingOptimizerEngine.class);

	private PunchProcessingThread punchProcessingThread;

	private Serializable optimizerParameters;

	private VirtualBook virtualBook;

	private OptimizersRepository repo;

	private PunchLayer punchLayer;

	private IssueLayer issueLayer;

	private ProcessingOptimizerEngineCallBack processingOptimizerEngineProgress;

	public ProcessingOptimizerEngine(ASyncConsoleOutput output,
			PunchLayer punchlayer, IssueLayer issueLayer,
			JIssuePresenter issuePresenter,
			OptimizersRepository optimizersRepository) {

		punchProcessingThread = new PunchProcessingThread(output, punchlayer,
				issuePresenter);

		punchProcessingThread
				.setPunchProcessingResultCallBack(new PunchProcessingResult() {
					@Override
					public void result(OptimizerResult result) {
						// transactional on the list itself
						punchLayer.setOptimizedObject(result.result);
						if (processingOptimizerEngineProgress != null)
							processingOptimizerEngineProgress
									.processEnded(result);
					}
				});

		assert optimizersRepository != null;
		repo = optimizersRepository;
		this.punchLayer = punchlayer;
		this.issueLayer = issueLayer;

	}

	public void setScrollableVirtualBook(JVirtualBookScrollableComponent jcarton) {
		punchProcessingThread.setJScrollableVirtualBookComponent(jcarton);
	}

	public void setAsyncManager(AsyncJobsManager asyncJobManager) {
		punchProcessingThread.setAsyncJobsManager(asyncJobManager);
	}

	public void changeVirtualBook(VirtualBook vb) throws Exception {
		this.virtualBook = vb;

		if (punchLayer != null && vb != null) {
			Scale s = vb.getScale();
			punchLayer.setPunchHeight(s.getTrackWidth());
		}

		reLaunchOptim();
	}

	public void changeOptimizerParameters(Serializable parameters)
			throws Exception {
		this.optimizerParameters = parameters;

		if (optimizerParameters != null
				&& optimizerParameters instanceof PunchConverterOptimizerParameters) {
			punchLayer
					.setPunchWidth(((PunchConverterOptimizerParameters) optimizerParameters)
							.getPunchWidth());
		}

		reLaunchOptim();
	}

	private ICancelTracker currentCancelTracker;

	private void reLaunchOptim() throws Exception {

		if (optimizerParameters != null && virtualBook != null) {

			Optimizer optim = repo
					.newOptimizerWithParameters(optimizerParameters);

			// stop previous process thread
			if (currentCancelTracker != null)
				currentCancelTracker.cancel();

			currentCancelTracker = new CancelTracker();

			issueLayer.setIssueCollection(new IssueCollection(), virtualBook);

			punchProcessingThread.execute(optim, virtualBook,
					currentCancelTracker, new OptimizerProgress() {

						@Override
						public void report(double progressIndicator,
								OptimizedObject[] orderedPunches, String message) {
							try {

								if (processingOptimizerEngineProgress != null) {
									processingOptimizerEngineProgress
											.progressUpdate(progressIndicator,
													message);
								}

							} catch (Exception ex) {
								logger.error(ex.getMessage(), ex);
							}
						}
					});
		}

	}

	public void setProcessingOptimizerEngineProgress(
			ProcessingOptimizerEngineCallBack processingOptimizerEngineProgress) {
		this.processingOptimizerEngineProgress = processingOptimizerEngineProgress;
	}

	public ProcessingOptimizerEngineCallBack getProcessingOptimizerEngineProgress() {
		return processingOptimizerEngineProgress;
	}

	public PunchLayer getPunchLayer() {
		return punchLayer;
	}

	public IssueLayer getIssueLayer() {
		return issueLayer;
	}

	public VirtualBook getVirtualBook() {
		return virtualBook;
	}

	@Override
	public void dispose() {
		try {
			// stop previous process thread
			if (currentCancelTracker != null)
				currentCancelTracker.cancel();
			
			currentCancelTracker = null;
			
		} catch (Exception ex) {
			logger.debug(
					"error disposing Processing Optimizer Engine"
							+ ex.getMessage(), ex);
		}
	}
}
