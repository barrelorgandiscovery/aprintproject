package org.barrelorgandiscovery.virtualbook.checker;

import java.util.Iterator;

import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.issues.IssueMissing;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Path;
import org.barrelorgandiscovery.tools.Point;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.transformation.TranspositionResult;


/**
 * Tool class
 * 
 * @author Freydiere Patrice
 * 
 */
public class TranspositionIssueConverter {

	/**
	 * search in the origine the nearest note approaching
	 * 
	 * @param book
	 * @param note
	 * @return -1 if not found
	 */
	private static int findInterpolatePosition(Scale book, NoteDef note) {

		// create a line path linked to the note positions ...

		AbstractTrackDef[] tds = book.getTracksDefinition();
		Path p = new Path();

		for (int i = 0; i < tds.length; i++) {
			AbstractTrackDef current = tds[i];
			if (current instanceof NoteDef) {
				NoteDef nd = (NoteDef) current;
				p.addPoint(new Point(i, nd.getMidiNote()));
			}
		}

		// find the intersecting point
		Point[] intersection = p.findIntersections(note.getMidiNote());
		if (intersection.length > 0) {
			// found
			return (int) intersection[0].getX(); // X is the track ...
		}

		return -1;
	}

	/**
	 * Create an issue collection from the transposition result
	 * 
	 * @param res
	 * @param origine
	 * @return
	 */
	public static IssueCollection convert(TranspositionResult res, Scale origine) {
		// conversion du resultat de transposition en
		if (res == null || res.untransposedholes == null)
			return null;

		IssueCollection issuecollection = new IssueCollection();

		for (Iterator<Hole> iterator = res.untransposedholes.iterator(); iterator
				.hasNext();) {

			Hole hole = (Hole) iterator.next();

			int interpolate = -1;

			AbstractTrackDef td = origine.getTracksDefinition()[hole.getTrack()];
			if (td instanceof NoteDef) {
				interpolate = findInterpolatePosition(res.virtualbook.getScale(),
						(NoteDef) td);
			}

			// recherche de la position interpolée pour les notes qui manquent

			issuecollection.add(new IssueMissing(hole.getTimestamp(), hole
					.getTimeLength(), td, interpolate));
		}

		return issuecollection;
	}
}
