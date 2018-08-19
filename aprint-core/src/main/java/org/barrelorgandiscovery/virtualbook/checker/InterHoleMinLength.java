package org.barrelorgandiscovery.virtualbook.checker;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.issues.IssueHole;
import org.barrelorgandiscovery.issues.IssuesConstants;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;


/**
 * This checker check that there is at least a minimum length between two
 * following holes
 */
public class InterHoleMinLength implements Checker {

	private static Logger logger = Logger.getLogger(InterHoleMinLength.class);
	
	private double interholeminlength;
	
	private double precision = 0.01; // 1 % of precision ...

	/**
	 * Constructor
	 * 
	 * @param interholeminlength
	 *            a minimum length between two holes
	 */
	public InterHoleMinLength(double interholeminlength) {
		this.interholeminlength = interholeminlength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.virtualbook.checker.Checker#check(fr.freydierepatrice.virtualbook.VirtualBook)
	 */
	public IssueCollection check(VirtualBook carton) throws Exception {

		IssueCollection ic = new IssueCollection();

		Scale g = carton.getScale();

		long l = g.mmToTime(interholeminlength - interholeminlength * precision);
		logger.debug("distance checked afterward (time):" + l);

		for (Iterator<Hole> iterator = carton.getOrderedHolesCopy().iterator(); iterator
				.hasNext();) {
			Hole hole = (Hole) iterator.next();

			// search for holes afterward

			List<Hole> intersected = carton
					.findHoles(hole.getTimestamp() + hole.getTimeLength() + 1, l,
							hole.getTrack(), hole.getTrack());

			if (intersected.size() > 0) {

				intersected.add(hole);

				ic.add(new IssueHole(IssuesConstants.INTERLEAVE_TOO_SHORT,
						(Hole[]) intersected.toArray(new Hole[0])));
			}

		}

		return ic;

	}
}
