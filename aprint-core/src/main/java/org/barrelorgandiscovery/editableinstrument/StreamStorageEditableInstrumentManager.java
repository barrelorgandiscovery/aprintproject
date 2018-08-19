package org.barrelorgandiscovery.editableinstrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;

public class StreamStorageEditableInstrumentManager implements
		EditableInstrumentManager {

	public static final String EDITABLE_INSTRUMENT_TYPE = "instrumentbundle"; //$NON-NLS-1$

	private static Logger logger = Logger
			.getLogger(StreamStorageEditableInstrumentManager.class);

	/**
	 * Stream storage
	 */
	private StreamStorage s = null;

	public StreamStorageEditableInstrumentManager(StreamStorage s) {
		assert s != null;
		this.s = s;
	}

	private Vector<EditableInstrumentManagerListener> listeners = new Vector<EditableInstrumentManagerListener>();

	public void fireInstrumentsChanged() {
		for (Iterator<EditableInstrumentManagerListener> iterator = listeners
				.iterator(); iterator.hasNext();) {
			EditableInstrumentManagerListener l = iterator.next();
			l.instrumentsChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentManager#addListener(fr.freydierepatrice.editableinstrument.EditableInstrumentManagerListener)
	 */
	public void addListener(EditableInstrumentManagerListener listener) {
		if (listener == null)
			return;

		listeners.add(listener);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentManager#removeListener(fr.freydierepatrice.editableinstrument.EditableInstrumentManagerListener)
	 */
	public void removeListener(EditableInstrumentManagerListener listener) {
		if (listener == null)
			return;

		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentManager#deleteEditableInstrument(java.lang.String)
	 */
	public void deleteEditableInstrument(String name) throws Exception {

		s.deleteStream(StringTools.removeExtension(name,
				EDITABLE_INSTRUMENT_TYPE), EDITABLE_INSTRUMENT_TYPE);

		fireInstrumentsChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentManager#listEditableInstruments()
	 */
	public String[] listEditableInstruments() throws Exception {
		return s.listStreams(EDITABLE_INSTRUMENT_TYPE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentManager#loadEditableInstrument(java.lang.String)
	 */
	public IEditableInstrument loadEditableInstrument(String name)
			throws Exception {

		InputStream is = s.openStream(name);

		return new EditableInstrumentStorage().load(is, name);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentManager#saveEditableInstrument(fr.freydierepatrice.editableinstrument.EditableInstrument)
	 */
	public void saveEditableInstrument(IEditableInstrument instrument)
			throws Exception {

		File tmpFile = File.createTempFile("tmp", ".st"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
			try {
				new EditableInstrumentStorage().save(instrument,
						fileOutputStream);
			} finally {
				fileOutputStream.close();
			}

			FileInputStream fileInputStream = new FileInputStream(tmpFile);
			try {
				s.saveStream(StringTools
						.convertToPhysicalNameWithEndingHashCode(instrument
								.getName()), EDITABLE_INSTRUMENT_TYPE,
						fileInputStream);
			} finally {
				fileInputStream.close();
			}
		} finally {
			tmpFile.delete();
		}
		fireInstrumentsChanged();
	}

	public String getEditableInstrumentDigest(String name) throws Exception {
		return s.getStreamDigest(name);
	}

	public Map<String, String> getAllEditableInstrumentDigests()
			throws Exception {

		HashMap<String, String> retvalue = new HashMap<String, String>();

		String[] instrumentList = listEditableInstruments();

		for (int i = 0; i < instrumentList.length; i++) {
			String string = instrumentList[i];
			retvalue.put(string, getEditableInstrumentDigest(string));
		}

		return retvalue;
	}

	/**
	 * Get the underlying stream storage ...
	 * 
	 * @return
	 */
	public StreamStorage getStreamStorage() {
		return s;
	}

}
