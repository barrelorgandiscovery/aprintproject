package org.barrelorgandiscovery.playsubsystem;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.playsubsystem.prepared.IPreparedCapableSubSystem;
import org.barrelorgandiscovery.playsubsystem.prepared.IPreparedPlaying;
import org.barrelorgandiscovery.playsubsystem.prepared.ISubSystemPlayParameters;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class ASyncPreparePlayin {

	private static Logger logger = Logger.getLogger(ASyncPreparePlayin.class);

	private volatile IPreparedPlaying alreadyProcessedPreparedPlaying = null;
	private volatile IPreparedCapableSubSystem pcss = null;

	private ExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();

	private AtomicInteger jobcounter = new AtomicInteger();

	private ConcurrentLinkedQueue<Callable> jobQueue = new ConcurrentLinkedQueue<Callable>();

	public void signalVirtualBookChanged(final IPreparedCapableSubSystem pcss,
			final VirtualBook vb) throws Exception {

		if (executor == null)
			throw new Exception("object has been disposed");
		
		if (vb == null) {
			return;
		}

		// reset
		alreadyProcessedPreparedPlaying = null;
		this.pcss = pcss;

		final int currentJob = jobcounter.incrementAndGet();
		System.out.println("launch playing prepare , current jobs # :"
				+ currentJob);

		final ISubSystemPlayParameters p = pcss.createParameterInstance();

		final VirtualBook clonedVB = vb.clone();

		Runnable callable = new Runnable() {

			public void run() {

				try {

					if (jobcounter.get() > currentJob) {
						System.out.println("abort job " + currentJob);
						return; // abort the job, because there are others
								// following
					}
					System.out.println("process job #:" + currentJob);

					IPreparedPlaying preparePlaying = pcss.preparePlaying(
							clonedVB, p);

					alreadyProcessedPreparedPlaying = preparePlaying;
					ASyncPreparePlayin.this.pcss = pcss;

				} catch (Exception ex) {
					logger.error(
							"error when processing prepareplaying :"
									+ ex.getMessage(), ex);
				}
			}
		};

		executor.execute(callable);

	}

	public void dispose() {
		if (executor != null)
			executor.shutdown();
		executor = null;
	}

	public IPreparedPlaying getComputedPreparedPlayin(
			IPreparedCapableSubSystem pcss) {
		if (this.pcss == pcss) {
			return alreadyProcessedPreparedPlaying;
		}

		return null;
	}

}
