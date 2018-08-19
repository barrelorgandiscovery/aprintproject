package org.barrelorgandiscovery.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;

/**
 * managing a repository collection ... to set a repository collection
 * 
 * @author Freydiere Patrice
 * 
 */
public class Repository2Collection implements Repository2, Disposable {

	private static Logger logger = Logger
			.getLogger(Repository2Collection.class);

	/**
	 * Repository Name
	 */
	private String name;

	public Repository2Collection(String name) {
		this.name = name;
	}

	@Override
	public String getLabel() {
		return Messages.getString("Repository2Collection.0"); //$NON-NLS-1$
	}
	
	
	private class InnerRepositoryListener implements RepositoryChangedListener {
		public void instrumentsChanged() {

			try {

				for (Iterator iterator = listeners.iterator(); iterator
						.hasNext();) {
					RepositoryChangedListener l = (RepositoryChangedListener) iterator
							.next();
					logger.debug("calling " + l); //$NON-NLS-1$
					l.instrumentsChanged();
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

		}

		public void scalesChanged() {

			try {
				for (Iterator iterator = listeners.iterator(); iterator
						.hasNext();) {
					RepositoryChangedListener l = (RepositoryChangedListener) iterator
							.next();
					logger.debug("calling " + l); //$NON-NLS-1$
					l.scalesChanged();
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

		}

		public void transformationAndImporterChanged() {
			try {

				for (Iterator iterator = listeners.iterator(); iterator
						.hasNext();) {
					RepositoryChangedListener l = (RepositoryChangedListener) iterator
							.next();
					logger.debug("calling " + l); //$NON-NLS-1$
					l.transformationAndImporterChanged();
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	private InnerRepositoryListener currentInnerListener = new InnerRepositoryListener();

	private List<Repository2> repositories = new ArrayList<Repository2>();

	public void addRepository(Repository2 repository) {
		repositories.add(repository);
		repository.addRepositoryChangedListener(currentInnerListener);

	}

	public Repository2 getRepository(int index) {
		return repositories.get(index);
	}

	public int getRepositoryCount() {
		return repositories.size();
	}

	public void removeRepository(Repository2 repository) {
		repository.removeRepositoryChangedListener(currentInnerListener);
		repositories.remove(repository);
	}

	private Vector<RepositoryChangedListener> listeners = new Vector<RepositoryChangedListener>();

	public void addRepositoryChangedListener(RepositoryChangedListener listener) {
		listeners.add(listener);
	}

	public void removeRepositoryChangedListener(
			RepositoryChangedListener listener) {
		listeners.remove(listener);
	}

	// implementation of repository 2

	public String getName() {
		return name;
	}

	/**
	 * return the merge of repositories readonly property
	 */
	public boolean isReadOnly() {
		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();
			if (!r.isReadOnly())
				return false;
		}
		return true;
	}

	public void deleteInstrument(Instrument instrument) throws Exception {

		if (instrument == null)
			return;

		Repository2 r = findRepositoryAssociatedTo(instrument);
		if (r == null)
			throw new Exception("no repository associated to " + instrument); //$NON-NLS-1$

	}

	public Instrument getInstrument(String name) {

		if (name == null)
			return null;

		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();
			Instrument instrument = r.getInstrument(name);
			if (instrument != null)
				return instrument;
		}

		logger.debug("instrument " + name + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
		return null;
	}

	public Instrument[] getInstrument(Scale scale) {

		if (scale == null)
			return new Instrument[0];

		ArrayList<Instrument> retvalue = new ArrayList<Instrument>();

		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();

			Instrument[] listInstruments = r.getInstrument(scale);
			for (int i = 0; i < listInstruments.length; i++) {
				Instrument instrument = listInstruments[i];
				retvalue.add(instrument);
			}
		}

		return retvalue.toArray(new Instrument[0]);
	}

	public Instrument[] listInstruments() {

		ArrayList<Instrument> retvalue = new ArrayList<Instrument>();

		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();

			Instrument[] listInstruments = r.listInstruments();
			for (int i = 0; i < listInstruments.length; i++) {
				Instrument instrument = listInstruments[i];
				retvalue.add(instrument);
			}
		}

		return retvalue.toArray(new Instrument[0]);
	}

	public void saveInstrument(Instrument instrument) throws Exception {
		Repository2 r = findDefaultWriteRepository();

		if (r == null)
			throw new Exception("no writable repository found ..."); //$NON-NLS-1$

		logger.debug("repository found for saving :" + r); //$NON-NLS-1$
		r.saveInstrument(instrument);
	}

	public void deleteScale(Scale scale) throws Exception {
		if (scale == null)
			return;

		Repository2 r = findRepositoryAssociatedTo(scale);

		if (r == null)
			throw new Exception("associated repository to " + scale //$NON-NLS-1$
					+ " not found"); //$NON-NLS-1$

		r.deleteScale(scale);
	}

	public Scale getScale(String name) {

		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();
			Scale s = r.getScale(name);
			if (s != null)
				return s;
		}

		logger.debug("scale " + name + " not found in repositories"); //$NON-NLS-1$ //$NON-NLS-2$
		return null;
	}

	public String[] getScaleNames() {

		ArrayList<String> retvalue = new ArrayList<String>();

		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();

			String[] scaleNames = r.getScaleNames();
			for (int i = 0; i < scaleNames.length; i++) {
				String string = scaleNames[i];
				retvalue.add(string);
			}
		}

		return retvalue.toArray(new String[0]);
	}

	public void saveScale(Scale scale) throws Exception {

		Repository2 r = findDefaultWriteRepository();
		if (r == null)
			throw new Exception("no writable repositories found"); //$NON-NLS-1$

		r.saveScale(scale);
	}

	public void deleteImporter(AbstractMidiImporter importer) throws Exception {

		Repository2 r = findRepositoryAssociatedTo(importer);
		if (r == null)
			throw new Exception("cannot found repository associated to " //$NON-NLS-1$
					+ importer);

		r.deleteImporter(importer);

	}

	public void deleteTransformation(AbstractTransformation transformation)
			throws Exception {

		throw new Exception("unsupported Operation"); //$NON-NLS-1$

	}

	public ArrayList<AbstractMidiImporter> findImporter(Scale destination) {

		logger.debug("findImporter " + destination); //$NON-NLS-1$

		if (destination == null)
			return new ArrayList<AbstractMidiImporter>();

		ArrayList<AbstractMidiImporter> retvalue = new ArrayList<AbstractMidiImporter>();

		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();
			ArrayList<AbstractMidiImporter> findImporter = r
					.findImporter(destination);
			retvalue.addAll(findImporter);
		}

		return retvalue;

	}

	public ArrayList<AbstractTransformation> findTransposition(Scale source,
			Scale destination) {

		logger.debug("findTransposition " + source + " -> " + destination); //$NON-NLS-1$ //$NON-NLS-2$

		if (destination == null)
			return new ArrayList<AbstractTransformation>();

		ArrayList<AbstractTransformation> retvalue = new ArrayList<AbstractTransformation>();

		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();
			ArrayList<AbstractTransformation> findTransposition = r
					.findTransposition(source, destination);
			retvalue.addAll(findTransposition);
		}

		return retvalue;
	}

	public void saveImporter(AbstractMidiImporter importer) throws Exception {
		Repository2 r = findDefaultWriteRepository();
		if (r == null)
			throw new Exception("no repository is writable"); //$NON-NLS-1$

		r.saveImporter(importer);
	}

	public void saveTransformation(AbstractTransformation transformation)
			throws Exception {
		Repository2 r = findDefaultWriteRepository();
		if (r == null)
			throw new Exception("no repository is writable"); //$NON-NLS-1$

		r.saveTransformation(transformation);

	}

	protected Repository2 findDefaultWriteRepository() {
		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();
			if (!r.isReadOnly())
				return r;
		}
		return null;
	}

	public Repository2 findRepositoryAssociatedTo(Scale scale) {
		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();

			Scale s = r.getScale(scale.getName());
			if (s == scale)
				return r;
		}
		return null;
	}

	public Repository2 findRepositoryAssociatedTo(Instrument instrument) {
		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();

			Instrument[] instruments = r.listInstruments();
			for (int i = 0; i < instruments.length; i++) {
				Instrument instrument2 = instruments[i];
				if (instrument2 == instrument)
					return r;
			}
		}
		return null;
	}

	public Repository2 findRepositoryAssociatedTo(AbstractMidiImporter importer) {
		if (importer == null)
			return null;

		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();

			ArrayList<AbstractMidiImporter> importersAssociatedToScale = r
					.findImporter(importer.getScaleDestination());
			for (Iterator iterator2 = importersAssociatedToScale.iterator(); iterator2
					.hasNext();) {
				AbstractMidiImporter abstractMidiImporter = (AbstractMidiImporter) iterator2
						.next();
				if (abstractMidiImporter == importer)
					return r;

			}
		}
		return null;
	}

	/**
	 * Find a repository by it's type
	 * 
	 * @param repositoryType
	 *            the wished repositories
	 * @return
	 */
	public <T> List<T> findRepository(Class<T> repositoryType) {
		ArrayList<T> ret = new ArrayList<T>();
		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 repository2 = (Repository2) iterator.next();

			if (repositoryType.isAssignableFrom(repository2.getClass())) {
				logger.debug("adding " + repository2); //$NON-NLS-1$
				ret.add((T)repository2);
			}

		}
		return ret;
	}

	public void dispose() {
		for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
			Repository2 r = (Repository2) iterator.next();
			if (r instanceof Disposable)
				((Disposable) r).dispose();
		}
	}

}
