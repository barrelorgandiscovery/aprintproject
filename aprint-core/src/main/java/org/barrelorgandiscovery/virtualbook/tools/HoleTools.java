package org.barrelorgandiscovery.virtualbook.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.barrelorgandiscovery.virtualbook.Hole;

public class HoleTools {

	/**
	 * move the hole collection to the given timestamp
	 * 
	 * @param holes
	 * @param timestamp
	 * @return
	 */
	public static Collection<Hole> moveHolesAt(Collection<Hole> holes, final long timestamp) {

		if (holes == null || holes.size() == 0)
			return new ArrayList<Hole>();

		TreeSet<Hole> ts = new TreeSet<>(holes);
		final long firstHoleTimeStamp = ts.first().getTimestamp();

		List<Hole> l = ts.stream().map(h -> {
			return new Hole(h.getTrack(), h.getTimestamp() - firstHoleTimeStamp + timestamp, h.getTimeLength());
		}).collect(Collectors.toList());

		return l;
	}

}
