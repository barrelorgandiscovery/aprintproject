package org.barrelorgandiscovery.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;

/**
 * Repository that filter the scales and instruments ..
 * 
 * @author Freydiere Patrice
 * 
 */
public class FilteredRepository implements Repository2, DerivedRepository {

	private static Logger logger = Logger.getLogger(FilteredRepository.class);

	private Repository2 r = null;
	private RepositoryTreeFilter filter = null;

	private class IdentityRepositoryFilter implements RepositoryTreeFilter {
		public boolean keepInstrument(Instrument instrument) {
			return true;
		}

		public boolean keepScale(Scale scale) {
			return true;
		}

	}

	public FilteredRepository(Repository2 r, RepositoryTreeFilter filter) {
		this.r = r;
		this.filter = filter;
		if (filter == null)
			this.filter = new IdentityRepositoryFilter();
		else
			logger.debug("using custom filter .. " + filter); //$NON-NLS-1$

	}

	public void addRepositoryChangedListener(RepositoryChangedListener listener) {
		r.addRepositoryChangedListener(listener);
	}

	public String getName() {
		return r.getName();
	}
	
	@Override
	public String getLabel() {
		return r.getLabel();
	}

	/**
	 * return the underlying readonly property
	 */
	public boolean isReadOnly() {
		return r.isReadOnly();
	}

	public void removeRepositoryChangedListener(
			RepositoryChangedListener listener) {
		r.removeRepositoryChangedListener(listener);
	}

	public void deleteInstrument(Instrument instrument) throws Exception {
		r.deleteInstrument(instrument);

	}

	public Instrument getInstrument(String name) {

		Instrument ins = r.getInstrument(name);

		if (!filter.keepInstrument(ins)) {
			logger.debug("remove " + ins); //$NON-NLS-1$
			return null;
		}
		return ins;
	}

	public Instrument[] getInstrument(Scale scale) {

		Instrument[] instruments = r.getInstrument(scale);
		List<Instrument> asList = new ArrayList<Instrument>(
				Arrays.asList(instruments));

		for (Instrument ins : instruments) {
			if (!filter.keepInstrument(ins)) {
				logger.debug("remove " + ins); //$NON-NLS-1$
				asList.remove(ins);
			}

		}

		return asList.toArray(new Instrument[0]);
	}

	public Instrument[] listInstruments() {
		Instrument[] instruments = r.listInstruments();
		List<Instrument> asList = new ArrayList<Instrument>(
				Arrays.asList(instruments));

		for (Instrument ins : instruments) {
			if (!filter.keepInstrument(ins)) {
				logger.debug("remove " + ins); //$NON-NLS-1$
				asList.remove(ins);
			}

		}

		return asList.toArray(new Instrument[0]);

	}

	public void saveInstrument(Instrument instrument) throws Exception {
		r.saveInstrument(instrument);
	}

	public void deleteScale(Scale scale) throws Exception {
		r.deleteScale(scale);
	}

	public Scale getScale(String name) {

		Scale s = r.getScale(name);
		if (!filter.keepScale(s))
			return null;

		return s;
	}

	public String[] getScaleNames() {

		String[] scaleNames = r.getScaleNames();
		List<String> asList = new ArrayList<String>(Arrays.asList(scaleNames));

		for (String sn : scaleNames) {

			Scale scale = r.getScale(sn);
			if (!filter.keepScale(scale)) {
				logger.debug("remove " + scale); //$NON-NLS-1$
				asList.remove(sn);
			}
		}

		return asList.toArray(new String[0]);
	}

	public void saveScale(Scale scale) throws Exception {
		r.saveScale(scale);

	}

	public void deleteImporter(AbstractMidiImporter importer) throws Exception {
		r.deleteImporter(importer);
	}

	public void deleteTransformation(AbstractTransformation transformation)
			throws Exception {
		r.deleteTransformation(transformation);
	}

	public ArrayList<AbstractMidiImporter> findImporter(Scale destination) {
		return r.findImporter(destination);
	}

	public ArrayList<AbstractTransformation> findTransposition(Scale source,
			Scale destination) {
		return r.findTransposition(source, destination);
	}

	public void saveImporter(AbstractMidiImporter importer) throws Exception {
		r.saveImporter(importer);
	}

	public void saveTransformation(AbstractTransformation transformation)
			throws Exception {
		r.saveTransformation(transformation);

	}

	/**
	 * Get the unfiltered repository collection
	 * 
	 * @return
	 */
	public Repository2 getRepository() {
		return this.r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.repository.DerivedRepository#getUnderlyingRepository
	 * ()
	 */
	public Repository2 getUnderlyingRepository() {
		return this.r;
	}

}
