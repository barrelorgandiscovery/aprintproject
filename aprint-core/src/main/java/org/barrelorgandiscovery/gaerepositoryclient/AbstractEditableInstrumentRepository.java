package org.barrelorgandiscovery.gaerepositoryclient;

import java.io.File;
import java.util.ArrayList;

import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;

/**
 * abstract base class for repository implementation
 * 
 * @author pfreydiere
 *
 */
public abstract class AbstractEditableInstrumentRepository implements Repository2, EditableInstrumentManagerRepository {

	protected EditableInstrumentManagerRepository2Adapter adapter;

	private EditableInstrumentManager manager;
	
	private String label;

	public AbstractEditableInstrumentRepository() {
		super();
	}

	protected void init(EditableInstrumentManager m, String name,String label, File cacheFolder) {
		this.manager = m;
	
		adapter = new EditableInstrumentManagerRepository2Adapter(getManager(), name,label, cacheFolder);

	}

	protected EditableInstrumentManager getManager() {
		return manager;
	}

	public void addRepositoryChangedListener(RepositoryChangedListener listener) {
		adapter.addRepositoryChangedListener(listener);
	}

	public String getName() {
		return adapter.getName();
	}

	@Override
	public String getLabel() {
		return label;
	}
	
	/**
	 * false
	 */
	public boolean isReadOnly() {
		return false;
	}

	public void removeRepositoryChangedListener(RepositoryChangedListener arg0) {
		adapter.removeRepositoryChangedListener(arg0);
	}

	public void deleteInstrument(Instrument arg0) throws Exception {
		adapter.deleteInstrument(arg0);
	}

	public Instrument getInstrument(String arg0) {
		return adapter.getInstrument(arg0);
	}

	public Instrument[] getInstrument(Scale arg0) {
		return adapter.getInstrument(arg0);
	}

	public Instrument[] listInstruments() {
		return adapter.listInstruments();
	}

	public void saveInstrument(Instrument arg0) throws Exception {
		adapter.saveInstrument(arg0);
	}

	public void deleteScale(Scale arg0) throws Exception {
		adapter.deleteScale(arg0);
	}

	public Scale getScale(String arg0) {
		return adapter.getScale(arg0);
	}

	public String[] getScaleNames() {
		return adapter.getScaleNames();
	}

	public void saveScale(Scale arg0) throws Exception {
		adapter.saveScale(arg0);
	}

	public void deleteImporter(AbstractMidiImporter arg0) throws Exception {
		adapter.deleteImporter(arg0);
	}

	public void deleteTransformation(AbstractTransformation arg0) throws Exception {
		adapter.deleteTransformation(arg0);
	}

	public ArrayList<AbstractMidiImporter> findImporter(Scale arg0) {
		return adapter.findImporter(arg0);
	}

	public ArrayList<AbstractTransformation> findTransposition(Scale arg0, Scale arg1) {
		return adapter.findTransposition(arg0, arg1);
	}

	public void saveImporter(AbstractMidiImporter arg0) throws Exception {
		adapter.saveImporter(arg0);
	}

	public void saveTransformation(AbstractTransformation arg0) throws Exception {
		adapter.saveTransformation(arg0);
	}

	public void dispose() {
		adapter.dispose();
	}

	public EditableInstrumentManager getEditableInstrumentManager() {
		return manager;
	}

	public String findEditableInstrumentName(String instrumentName) {
		return adapter.findAssociatedEditableInstrument(instrumentName);
	}

	public String findAssociatedEditableInstrumentName(String instrumentname) {
		return adapter.findAssociatedEditableInstrument(instrumentname);
	}

	/**
	 * for derivative class, signal the instruments changed to refresh the gui
	 */
	protected void fireInstrumentsChanged() {
		adapter.signalInstrumentChanged();
	}

}