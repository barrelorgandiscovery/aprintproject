package org.barrelorgandiscovery.repository;

import java.util.ArrayList;

import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.instrument.InstrumentManager;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;
import org.barrelorgandiscovery.virtualbook.transformation.TransformationManager;


/**
 * Translate a repository2 to a repository class (for existing developpement),
 * pattern adapters
 * 
 * @author Freydiere Patrice
 * 
 */
public class RepositoryAdapter implements Repository {

	private Repository2 rep;

	public RepositoryAdapter(Repository2 rep2) {
		this.rep = rep2;
	}

	public Repository2 getRepository2() {
		return this.rep;
	}
	
	@Override
	public String getLabel() {
		return rep.getLabel();
	}

	private class InstrumentManagerAdapter implements InstrumentManager {

		public void deleteInstrument(Instrument instrument) throws Exception {
			rep.deleteInstrument(instrument);
		}

		public Instrument getInstrument(String name) {
			return rep.getInstrument(name);
		}

		public Instrument[] getInstrument(Scale gamme) {
			return rep.getInstrument(gamme);
		}

		public Instrument[] listInstruments() {
			return rep.listInstruments();
		}

		public void saveInstrument(Instrument instrument) throws Exception {
			rep.saveInstrument(instrument);
		}
	}

	private InstrumentManagerAdapter ima = new InstrumentManagerAdapter();

	public InstrumentManager getInstrumentManager() {
		return ima;
	}

	private class ScaleManagerAdapter implements ScaleManager {

		public void deleteScale(Scale scale) throws Exception {
			rep.deleteScale(scale);
		}

		public Scale getScale(String name) {
			return rep.getScale(name);

		}

		public String[] getScaleNames() {
			return rep.getScaleNames();
		}

		public void saveScale(Scale scale) throws Exception {
			rep.saveScale(scale);
		}

	}

	private ScaleManagerAdapter sma = new ScaleManagerAdapter();

	public ScaleManager getScaleManager() {
		return sma;
	}

	private class TransformationManagerAdapter implements TransformationManager {

		public void deleteImporter(AbstractMidiImporter importer)
				throws Exception {
			rep.deleteImporter(importer);

		}

		public void deleteTransformation(AbstractTransformation transformation)
				throws Exception {
			rep.deleteTransformation(transformation);
		}

		public ArrayList<AbstractMidiImporter> findImporter(Scale destination) {
			return rep.findImporter(destination);
		}

		public ArrayList<AbstractTransformation> findTransposition(
				Scale source, Scale destination) {
			return rep.findTransposition(source, destination);
		}

		public void saveImporter(AbstractMidiImporter importer)
				throws Exception {
			rep.saveImporter(importer);
		}

		public void saveTransformation(AbstractTransformation transformation)
				throws Exception {
			rep.saveTransformation(transformation);
		}

	}

	private TransformationManagerAdapter tma = new TransformationManagerAdapter();

	public TransformationManager getTranspositionManager() {
		return tma;
	}

	public String getName() {
		return rep.getName();
	}

}
