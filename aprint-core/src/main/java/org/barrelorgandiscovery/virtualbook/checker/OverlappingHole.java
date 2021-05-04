package org.barrelorgandiscovery.virtualbook.checker;

import java.util.Iterator;
import java.util.List;

import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.issues.IssueHole;
import org.barrelorgandiscovery.issues.IssuesConstants;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;


/**
 * Check for overlapping holes
 * 
 * @author Freydiere Patrice
 * 
 */
public class OverlappingHole implements Checker {

	/**
	 * Constructor
	 */
	public OverlappingHole() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.virtualbook.checker.Checker#check(fr.freydierepatrice.virtualbook.VirtualBook)
	 */
	public IssueCollection check(VirtualBook carton) throws Exception {

		// Parcourt du carton pour rechercher les notes qui se téléscopent

		IssueCollection ic = new IssueCollection();

		for (Iterator<Hole> iterator = carton.getOrderedHolesCopy().iterator(); iterator
				.hasNext();) {
			Hole hole = iterator.next();

			// recherche des notes intersectant ...

			List<Hole> intersectedHole = carton.findHoles(hole
					.getTimestamp(), hole.getTimeLength(), hole.getTrack(), hole
					.getTrack());

			if (intersectedHole != null && intersectedHole.size() > 1) {
				// ajout de l'erreur ...
				ic.add(new IssueHole(IssuesConstants.OVERLAPPING_HOLE,
						new Hole[] { hole }));
			}

		}

		return ic;
	}

}
