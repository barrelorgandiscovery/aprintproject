package org.barrelorgandiscovery.virtualbook.checker;

import java.util.Iterator;

import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.issues.IssueHole;
import org.barrelorgandiscovery.issues.IssuesConstants;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;


/**
 * Check for too short holes ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class TooShortHole implements Checker {

	/**
	 * minimum lenght for holes in mm
	 */
	private double minlength;

	/**
	 * Constructor 
	 * 
	 * @param minlength
	 *            minimum length for hole in mm
	 */
	public TooShortHole(double minlength) {
		this.minlength = minlength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.checker.Checker#check(fr.freydierepatrice.cartonvirtuel.CartonVirtuel)
	 */
	public IssueCollection check(VirtualBook carton) throws Exception {

		IssueCollection ic = new IssueCollection();

		Scale g = carton.getScale();

		for (Iterator<Hole> iterator = carton.getOrderedHolesCopy().iterator(); iterator
				.hasNext();) {
			Hole hole = (Hole) iterator.next();

			if (g.timeToMM(hole.getTimeLength()) < minlength) {
				ic.add(new IssueHole(IssuesConstants.HOLE_TOO_SMALL,
						new Hole[] { hole }));
			}

		}

		return ic;
	}
}
