package org.barrelorgandiscovery;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.Disposable;

/**
 * Class managing asynchronous jobs, this class reference all threaded actions,
 * and propose an async execution of some jobs
 * 
 */
public class AsyncJobsManager implements Disposable {
	private static Logger logger = Logger.getLogger(AsyncJobsManager.class);

	/**
	 * inner executor that periodically check the availability of the end of a
	 * job
	 */
	private ScheduledExecutorService execService = Executors
			.newScheduledThreadPool(3,new ThreadFactory() {
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setPriority(Thread.MIN_PRIORITY);
					return t;
				}
			});

	/**
	 * Executor for submitting jobs
	 */
	private ExecutorService exec = Executors.newSingleThreadExecutor();

	/**
	 * Tracked jobs
	 */
	private ConcurrentLinkedQueue<Job> jobs = new ConcurrentLinkedQueue<Job>();

	public AsyncJobsManager() {

		execService.scheduleAtFixedRate(new Runnable() {
			public void run() {

				if (jobs.size() == 0)
					return;

				ArrayList<Job> jobsToRemove = new ArrayList<Job>();

				for (Iterator iterator = jobs.iterator(); iterator.hasNext();) {
					Job j = (Job) iterator.next();

					Future future = j.future;
					if (future != null) {

						if (future.isDone()) {
							jobsToRemove.add(j);
							final JobEvent cb = j.callback;
							if (cb != null) {
								try {

									final Object result = future.get();
									logger.debug("job " + j + " finished");
									if (cb != null) {
										SwingUtilities
												.invokeLater(new Runnable() {
													public void run() {
														cb.jobFinished(result);
													}
												});

									}
								} catch (final Exception ex) {
									logger.error(
											"error launching the job callback :"
													+ ex.getMessage(), ex);
									if (cb != null) {
										SwingUtilities
												.invokeLater(new Runnable() {
													public void run() {
														cb.jobError(ex);
													}
												});

									}
								}
							}
						}
					}
				}

				for (Iterator iterator2 = jobsToRemove.iterator(); iterator2
						.hasNext();) {
					Job job = (Job) iterator2.next();
					jobs.remove(job);
				}

			}
		}, 200, 200, TimeUnit.MILLISECONDS);

	}

	/**
	 * Submit an asynchrone Job
	 * 
	 * @param f
	 *            the job to do, might be cancellable
	 * @param e
	 *            the object who is called when events occurs in the processing
	 */
	public void submitAlreadyExecutedJobToTrack(Future f, JobEvent e) {
		Job j = new Job();
		j.future = f;
		j.callback = e;
		jobs.add(j);
	}

	/**
	 * Execute and submit the asynchronous job
	 * 
	 * @param f
	 * @param e
	 */
	public void submitAndExecuteJob(Callable f, JobEvent e) {
		Future future = exec.submit(f);
		Job j = new Job();
		j.future = future;
		j.callback = e;
		jobs.add(j);
	}

	/**
	 * submit helper for groovy code
	 * 
	 * @param job
	 *            the job to be done
	 * @param successOrErrorCallBack
	 *            in case of success or error, call this closure
	 * @since 2011.6
	 */
	public void submit(final Closure job, final Closure successOrErrorCallBack) {
		submit(job, successOrErrorCallBack, successOrErrorCallBack);
	}

	/**
	 * submit helper for groovy code
	 * 
	 * @param job
	 *            the job to be done
	 * @param successCallBack
	 *            in case of success, call this closure
	 * @param errorCallBack
	 *            in case of error, call this closure
	 * @since 2011.6
	 */
	public void submit(final Closure job, final Closure successCallBack,
			final Closure errorCallBack) {

		final Job j = new Job();

		final Future ft = exec.submit(new Callable() {
			public Object call() throws Exception {
				return job.call();
			}
		});

		j.future = ft;
		j.callback = new JobEvent() {

			public void jobFinished(Object result) {
				try {
					if (successCallBack != null)
						successCallBack.call(result);
				} catch (Throwable t) {
					logger.error(
							"error in calling job finished :" + t.getMessage(),
							t);
				}
			}

			public void jobError(Exception ex) {
				try {
					if (errorCallBack != null)
						errorCallBack.call(ex);
				} catch (Throwable t) {
					logger.error(
							"error in calling job error callback :"
									+ t.getMessage(), t);
				}

			}

			public void jobAborted() {
				
			}
		};
		jobs.add(j);
	}

	public void dispose() {
		execService.shutdownNow();
		logger.debug("Jobs shutdowned");
	}

}
