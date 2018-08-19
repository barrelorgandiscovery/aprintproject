package org.barrelorgandiscovery.gaerepositoryclient.synchroreport;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Report for the synchronization result
 * 
 * @author Freydiere Patrice
 * 
 */
public class SynchronizationReport extends ArrayList<SynchroElement> {

	public SynchronizationReport() {
	}

	public boolean hasErrors() {
		for (Iterator<SynchroElement> it = this.iterator(); it.hasNext();) {
			SynchroElement e = it.next();

			if (e.getStatus() == SynchroElement.ERROR)
				return true;

		}
		return false;
	}
}
