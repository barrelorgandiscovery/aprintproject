package org.barrelorgandiscovery.repository;

import java.util.ArrayList;
import java.util.Iterator;

import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.instrument.InstrumentManager;
import org.barrelorgandiscovery.instrument.StorageInstrumentManager;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.scale.StorageScaleManager;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;
import org.barrelorgandiscovery.virtualbook.transformation.StorageTransformationManager;
import org.barrelorgandiscovery.virtualbook.transformation.TransformationManager;

/**
 * Implementation of a repository based on a storage implementation
 * 
 * @author use
 * 
 */
public class StorageRepositoryImpl implements Repository2 {

	private StreamStorage fis;

	private ScaleManager gm;
	private TransformationManager tm;
	private InstrumentManager im;
	private String name;
	private String label;

	StorageRepositoryImpl(StreamStorage storage, String name, String label)
			throws RepositoryException {

		fis = storage;
		this.name = name;
		this.label = label;

		gm = new StorageScaleManager(fis);
		tm = new StorageTransformationManager(fis, gm);
		im = new StorageInstrumentManager(fis, gm);
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public String getLabel() {
		if (label != null && !label.isEmpty()) {
			return label;
		}
		return name;
	}

	public Instrument getInstrument(String name) {
		return im.getInstrument(name);
	}

	public Instrument[] getInstrument(Scale gamme) {
		return im.getInstrument(gamme);
	}

	public Instrument[] listInstruments() {
		return im.listInstruments();
	}

	public String[] getScaleNames() {
		return gm.getScaleNames();
	}

	public Scale getScale(String name) {
		return gm.getScale(name);
	}

	public ArrayList<AbstractMidiImporter> findImporter(Scale destination) {
		return tm.findImporter(destination);
	}

	public ArrayList<AbstractTransformation> findTransposition(Scale source,
			Scale destination) {

		return tm.findTransposition(source, destination);
	}

	private ArrayList<RepositoryChangedListener> listeners = new ArrayList<RepositoryChangedListener>();

	public void addRepositoryChangedListener(RepositoryChangedListener listener) {
		assert listener != null;
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	protected void fireScalesChanged() {
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
			RepositoryChangedListener l = (RepositoryChangedListener) iterator
					.next();
			l.scalesChanged();
		}
	}

	protected void fireInstrumentsChanged() {
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
			RepositoryChangedListener l = (RepositoryChangedListener) iterator
					.next();
			l.instrumentsChanged();
		}
	}

	protected void fireTransformationAndImporterChanged() {
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
			RepositoryChangedListener l = (RepositoryChangedListener) iterator
					.next();
			l.transformationAndImporterChanged();
		}
	}

	public void removeRepositoryChangedListener(
			RepositoryChangedListener listener) {
		listeners.remove(listener);
	}

	public void saveImporter(AbstractMidiImporter importer) throws Exception {
		tm.saveImporter(importer);
		fireTransformationAndImporterChanged();
	}

	public void deleteImporter(AbstractMidiImporter importer) throws Exception {
		tm.deleteImporter(importer);
		fireTransformationAndImporterChanged();
	}

	public void deleteInstrument(Instrument instrument) throws Exception {
		im.deleteInstrument(instrument);
		fireInstrumentsChanged();
	}

	public void saveScale(Scale scale) throws Exception {
		gm.saveScale(scale);
		fireScalesChanged();
	}

	public void deleteScale(Scale scale) throws Exception {
		gm.deleteScale(scale);
		fireScalesChanged();

	}

	public void deleteTransformation(AbstractTransformation transformation)
			throws Exception {
		tm.deleteTransformation(transformation);
		fireTransformationAndImporterChanged();
	}

	public void saveInstrument(Instrument instrument) throws Exception {
		im.saveInstrument(instrument);
		fireInstrumentsChanged();
	}

	public void saveTransformation(AbstractTransformation transformation)
			throws Exception {
		tm.saveTransformation(transformation);
		fireTransformationAndImporterChanged();
	}

	public boolean isReadOnly() {
		return this.fis.isReadOnly();
	}

}
