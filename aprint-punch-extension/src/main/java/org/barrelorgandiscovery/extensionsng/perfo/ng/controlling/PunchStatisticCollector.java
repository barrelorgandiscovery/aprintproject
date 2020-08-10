package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

import java.util.LinkedList;
import java.util.Queue;

/**
 * this class collect the statistics and associated metrics
 * 
 * @author pfreydiere
 * 
 */
public class PunchStatisticCollector {
	
	private static final int MAX_MEASURES = 10000;

	private double distance = Double.NaN;
	private Queue<Double> punchTime = new LinkedList<Double>();
	private Queue<Double> displacement = new LinkedList<Double>();

	private long startTime;
	private long queueSize;

	public PunchStatisticCollector(int queueSize) {
		this.queueSize = queueSize;
	}

	public void resetDistance(double distanceMax) {
		this.distance = distanceMax;
	}

	public synchronized void informStartPunch() {
		startTime = System.currentTimeMillis();
	}

	public synchronized void informPunchStopped(double distance) {

		while ((punchTime.size() > MAX_MEASURES && displacement.size() > 0)
				|| (displacement.size() > MAX_MEASURES && punchTime.size() > 0)) {
			punchTime.poll();
			displacement.poll();
		}
		
		if (queueSize-- < 0) {
			punchTime.add((System.currentTimeMillis() - startTime) / 1000.0);
		}

		displacement.add(distance);

		this.distance -= distance;

	}

	public synchronized double getMeanTimePerPunch() {

		int nbv = 0;
		double acc = 0.0;
		
		for (Double d : punchTime) {
			if (d == null)
				continue;

			nbv++;
			acc += d;
		}

		if (nbv == 0)
			return 0;

		return acc / nbv;

	}

	public double getDistanceLeft() {
		return this.distance;
	}

}
