package org.barrelorgandiscovery.repository;

import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.scale.Scale;

public interface RepositoryTreeFilter {

	public boolean keepInstrument(Instrument instrument);

	public boolean keepScale(Scale scale);

}
