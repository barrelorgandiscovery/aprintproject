package org.barrelorgandiscovery.scale.diff;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.scale.AbstractRegisterCommandDef;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.ControlTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.eval.Note;

/**
 * Comparateur de gamme
 * 
 * @author pfreydiere
 * 
 */
public class ScaleDifferentiator {

	/**
	 * compare getters
	 * 
	 * @param s1
	 * @param s2
	 * @param propertyName
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private boolean compareAndAddDimensionDiff(Scale s1, Scale s2, String propertyName,
			List<AbstractDiffElement> result) throws Exception {

		Method m = s1.getClass().getMethod("get" + propertyName);

		Double source = (Double) m.invoke(s1);
		Double destination = (Double) m.invoke(s1);

		assert source != null;
		assert destination != null;

		if (!source.equals(destination)) {
			result.add(new DimensionDiff(propertyName, source, destination));
			return true;
		}

		return false;
	}

	private boolean compareControlProperties(int track, ControlTrackDef source, ControlTrackDef destination,
			List<AbstractDiffElement> result) {
		boolean r = true;

		if (!((Double) source.getRetard()).equals(destination.getRetard())) {
			result.add(new DelayDiff(track));
			r = false;
		}

		if (!((Double) source.getLength()).equals(destination.getLength())) {
			result.add(new FixedLengthDiff(track));
			r = false;
		}

		return r;
	}

	public List<AbstractDiffElement> diff(Scale s1, Scale s2) throws Exception {

		assert s1 != null;
		assert s2 != null;

		// construct result
		ArrayList<AbstractDiffElement> result = new ArrayList<AbstractDiffElement>();

		// list properties for checking
		String[] dimensionProperties = new String[] { "Width", "IntertrackHeight", "TrackWidth", "FirstTrackAxis",
				"Speed" };
		for (int i = 0; i < dimensionProperties.length; i++) {
			String prop = dimensionProperties[i];
			compareAndAddDimensionDiff(s1, s2, prop, result);
		}

		if (s1.getTrackNb() != s2.getTrackNb()) {
			result.add(new TrackNumberDiff(s1.getTrackNb(), s2.getTrackNb()));
		}

		// compare pistes
		AbstractTrackDef[] tdsource = s1.getTracksDefinition();
		AbstractTrackDef[] tddest = s2.getTracksDefinition();

		assert tdsource != null;
		for (int i = 0; i < tdsource.length; i++) {
			AbstractTrackDef aTrackDefSource = tdsource[i];
			if (aTrackDefSource == null) {
				// no definition for the track
				// skip
				continue;
			}

			if (i >= tddest.length) {
				// en dehors de la plage
				result.add(new TrackDiff(i));
				// pas de correspondance
				continue;
			}

			AbstractTrackDef aTrackDefDest = tddest[i];
			if (aTrackDefDest == null) {
				// la destination est null !!
				// il n'y a pas de correspondance pour le track
				result.add(new TrackDiff(i));
				continue;
			}

			assert aTrackDefDest != null;

			// vérification du type

			if (aTrackDefSource instanceof NoteDef) {
				// c'est une note

				NoteDef n1 = (NoteDef) aTrackDefSource;

				if (!(aTrackDefDest instanceof NoteDef)) {
					result.add(new TrackTypeMismatched(i));
					continue;
				}

				NoteDef n2 = (NoteDef) aTrackDefDest;

				Note s = new Note(n1);
				Note d = new Note(n2);

				// compare note
				if (!s.hasSameMidiCode(d)) {
					if (s.isSameSimpleNoteAs(d)) {
						// pas la même octave
						result.add(new NoteOctaveDiff(i, s, d));
						continue;
					} else {
						result.add(new NoteDiff(i, s, d));
						continue;
					}
				}
				// même code

				continue;

			} else if (aTrackDefSource instanceof PercussionDef) {

				PercussionDef spd = (PercussionDef) aTrackDefSource;
				if (!(aTrackDefDest instanceof PercussionDef)) {
					result.add(new TrackTypeMismatched(i));
					continue;
				}

				PercussionDef dpd = (PercussionDef) aTrackDefDest;

				if (dpd.getPercussion() != spd.getPercussion()) {
					result.add(new PercussionDiff(i));
					continue;
				}

				// check caracteristics

				compareControlProperties(i, spd, dpd, result);

			} else if (aTrackDefSource instanceof AbstractRegisterCommandDef) {

				if (!(aTrackDefDest.equals(aTrackDefSource))) {
					result.add(new RegisterCommandMismatchedDiff(i));
					continue;
				}

			} else {
				throw new Exception("unsupported trackdef definition :" + aTrackDefSource.getClass());
			}

		}

		// compare registers, if exists

		return result;
	}

}
