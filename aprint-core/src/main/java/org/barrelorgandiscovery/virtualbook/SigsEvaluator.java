package org.barrelorgandiscovery.virtualbook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.barrelorgandiscovery.virtualbook.sigs.ComputedSig;

public class SigsEvaluator {

	/**
	 * This method compute the sigs from an event list
	 * 
	 * @param events
	 * @return
	 */
	public List<ComputedSig> computeSigs(Set<AbstractEvent> events) {

		long currentBeatNumberPerSig = 4; // 4/4 by default
		long currentBeatLength = 1000000;

		List<ComputedSig> sigs = new ArrayList<ComputedSig>();
		ComputedSig c = new ComputedSig();
		c.measureLength = currentBeatLength * currentBeatNumberPerSig;
		c.sigNumber = 0;
		c.timeStamp = 0;

		// the current tempo modified Sig

		long evaluatedNextMesureLength = c.measureLength;
		long lastmodifiedTimeStampSig = -1;

		
		// browsing in current order the sigs
		for (Iterator iterator = events.iterator(); iterator.hasNext();) {

			AbstractEvent abstractEvent = (AbstractEvent) iterator.next();

			if (abstractEvent instanceof TempoChangeEvent
					|| abstractEvent instanceof SignatureEvent) {
				// check point ..
				if (c.timeStamp + evaluatedNextMesureLength <= abstractEvent
						.getTimestamp()) {

					// push, if tempo has changed, adjust the measure length
					if (lastmodifiedTimeStampSig > 0) {
						// tempo has been modified previously
						c.measureLength = evaluatedNextMesureLength;
						lastmodifiedTimeStampSig = -1;

					}

					sigs.add(c);
					ComputedSig nc = new ComputedSig();
					// nc.measureLength

					// number of sigs ...
					int n = (int) ((abstractEvent.getTimestamp() - c.timeStamp) / c.measureLength);

					nc.timeStamp = c.timeStamp + (n * c.measureLength);
					nc.measureLength = c.measureLength;
					nc.sigNumber = c.sigNumber + n;

					evaluatedNextMesureLength = c.measureLength;

					c = nc;

				}

			}

			if (abstractEvent instanceof TempoChangeEvent) {
				TempoChangeEvent tc = (TempoChangeEvent) abstractEvent;

				if (tc.getTimestamp() == c.timeStamp) {

					// update current next elements
					c.measureLength = tc.getNoirLength()
							* currentBeatNumberPerSig;
					evaluatedNextMesureLength = c.measureLength;
					currentBeatLength = tc.getNoirLength();
					lastmodifiedTimeStampSig = -1;

				} else {

					// tempo already modified
					// last modified
					long delta = tc.getTimestamp() - (c.timeStamp);

					long left = evaluatedNextMesureLength - delta;
					assert left >= 0;

					left = (long) (left * ((1.0 * tc.getNoirLength()) / currentBeatLength));

					// left = (long) (1.0 * left * (1.0 * currentBeatLength /
					// tc.getNoirLength()));

					evaluatedNextMesureLength = delta + left;
					lastmodifiedTimeStampSig = tc.getTimestamp();
					currentBeatLength = tc.getNoirLength();

				}

			} else if (abstractEvent instanceof SignatureEvent) {

				SignatureEvent se = (SignatureEvent) abstractEvent;

				if (se.getTimestamp() == c.timeStamp) {
					currentBeatNumberPerSig = se.getNumerateur();
					evaluatedNextMesureLength = currentBeatNumberPerSig
							* currentBeatLength;
					c.measureLength = evaluatedNextMesureLength;

				} else {
					// push
					sigs.add(c);
					ComputedSig nc = new ComputedSig();
					// nc.measureLength

					currentBeatNumberPerSig = se.getNumerateur();

					nc.timeStamp = se.getTimestamp();
					nc.measureLength = currentBeatNumberPerSig
							* currentBeatLength;
					nc.sigNumber = c.sigNumber + 1;

					evaluatedNextMesureLength = nc.measureLength;

					c = nc;
				}
			}

		}
		if (lastmodifiedTimeStampSig > 0)
			c.measureLength = evaluatedNextMesureLength;

		sigs.add(c);

		return sigs;
	}

}
