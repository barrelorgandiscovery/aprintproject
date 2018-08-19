package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.util.Comparator;

public class MidiAdvancedEventTimestampComparator implements
		Comparator<MidiAdvancedEvent> {

	public int compare(MidiAdvancedEvent o1, MidiAdvancedEvent o2) {

		if (o1 == o2)
			return 0;

		long firstTick = o1.getTimeStamp();
		long secondTick = o2.getTimeStamp();
		if (firstTick > secondTick) {
			return 1;
		} else if (firstTick == secondTick) {
			return ((Integer) o1.hashCode()).compareTo(o2.hashCode());
		} else {
			return -1;
		}
	}

}
