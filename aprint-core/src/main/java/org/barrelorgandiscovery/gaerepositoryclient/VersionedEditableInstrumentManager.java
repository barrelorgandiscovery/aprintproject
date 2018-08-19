package org.barrelorgandiscovery.gaerepositoryclient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerListener;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentStorage;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;

/**
 * This class manage a versionned instrument repository, maintining modified,
 * new instrument created in local for synchronizing with external sources
 * 
 * @author use
 * 
 */
public class VersionedEditableInstrumentManager implements
		EditableInstrumentManager, Disposable {

	private Logger logger = Logger
			.getLogger(VersionedEditableInstrumentManager.class);

	private StreamStorage streamStorage;

	public final static String SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE = "versionedinstrumentbundle"; //$NON-NLS-1$

	public VersionedEditableInstrumentManager(StreamStorage streamStorage) {
		this.streamStorage = streamStorage;
	}

	private Vector<EditableInstrumentManagerListener> listeners = new Vector<EditableInstrumentManagerListener>();

	public void addListener(EditableInstrumentManagerListener listener) {
		if (listener != null)
			listeners.add(listener);
	}

	public void removeListener(EditableInstrumentManagerListener listener) {
		listeners.remove(listener);
	}

	protected void fireInstrumentsChanged() {
		logger.debug("fire Instruments Changed"); //$NON-NLS-1$
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
			EditableInstrumentManagerListener l = (EditableInstrumentManagerListener) iterator
					.next();
			l.instrumentsChanged();
		}
	}

	private String constructStreamName(DecodedInstrumentRef r) {
		return "" + r.status + "_" + r.instrumentID + "_" + r.version + "_" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ StringTools.convertToPhysicalName(r.name)
				+ "." + SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE; //$NON-NLS-1$
	}

	public HashMap<String, DecodedInstrumentRef> readDecodedStreamReferences()
			throws Exception {

		logger.debug("readDecodedStreamReferences"); //$NON-NLS-1$
		String[] streams = streamStorage
				.listStreams(SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);

		Pattern p = Pattern.compile("([NUDF])_([\\-0-9]+)_([\\-0-9]+)_(.*)\\." //$NON-NLS-1$
				+ SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);

		HashMap<String, DecodedInstrumentRef> references = new HashMap<String, DecodedInstrumentRef>();

		for (int i = 0; i < streams.length; i++) {
			String streamName = streams[i];
			logger.debug("evaluate stream :" + streamName); //$NON-NLS-1$

			try {
				assert streamName.endsWith("." //$NON-NLS-1$
						+ SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);
				Matcher m = p.matcher(streamName);

				if (!m.matches()) {
					logger.debug("stream " + streamName + " doesn't match"); //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}

				logger.debug("analysing " + streamName); //$NON-NLS-1$

				String status = m.group(1);
				String instrumentid = m.group(2);
				String version = m.group(3);
				String name = m.group(4);

				DecodedInstrumentRef r = new DecodedInstrumentRef();
				r.fullStreamName = streamName;
				r.status = status;
				r.version = Long.parseLong(version);
				r.instrumentID = Long.parseLong(instrumentid);
				r.name = name;

				String key = (r.instrumentID < 0 ? "new" : "" + r.instrumentID) //$NON-NLS-1$ //$NON-NLS-2$
						+ "_" + name; //$NON-NLS-1$

				if (references.containsKey(key))
					throw new Exception("key already exist ..."); //$NON-NLS-1$

				references.put(key, r);

			} catch (Exception ex) {
				logger.error("Stream " + streamName //$NON-NLS-1$
						+ "skipped because of an error " + ex.getMessage(), ex); //$NON-NLS-1$
			}
		}

		return references;
	}

	public String[] listEditableInstruments() throws Exception {
		logger.debug("listEditableInstruments"); //$NON-NLS-1$

		ArrayList<String> retvalue = new ArrayList<String>();

		HashMap<String, DecodedInstrumentRef> decoded = readDecodedStreamReferences();

		for (Iterator<Entry<String, DecodedInstrumentRef>> iterator = decoded
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, DecodedInstrumentRef> entry = iterator.next();

			DecodedInstrumentRef d = entry.getValue();
			if (!"D".equals(d.status)) { //$NON-NLS-1$
				retvalue.add(entry.getKey());
			}
		}

		return retvalue.toArray(new String[0]);
	}

	public IEditableInstrument loadEditableInstrument(String reference)
			throws Exception {

		HashMap<String, DecodedInstrumentRef> decoded = readDecodedStreamReferences();

		if (!decoded.containsKey(reference))
			throw new Exception("unknown instrument reference"); //$NON-NLS-1$

		DecodedInstrumentRef r = decoded.get(reference);

		InputStream is = streamStorage.openStream(r.fullStreamName);

		IEditableInstrument editableInstrument = new EditableInstrumentStorage()
				.load(is, r.name);

		SynchronizedEditableInstrument sinstrument = new SynchronizedEditableInstrument(
				editableInstrument, r.instrumentID, r.version, r.status,
				reference);

		return sinstrument;
	}

	public void saveEditableInstrument(IEditableInstrument instrument)
			throws Exception {

		if (instrument instanceof SynchronizedEditableInstrument) {
			// instrument to synchronize

			HashMap<String, DecodedInstrumentRef> decoded = readDecodedStreamReferences();

			SynchronizedEditableInstrument sei = (SynchronizedEditableInstrument) instrument;

			String instrumentReference = sei.getReferenceName();

			if (!decoded.containsKey(instrumentReference))
				throw new Exception("unknown instrument reference"); //$NON-NLS-1$

			DecodedInstrumentRef decodedInstrumentRef = decoded
					.get(instrumentReference);

			logger.debug("status of the saved instrument :" //$NON-NLS-1$
					+ decodedInstrumentRef.status);

			if ("F".equals(decodedInstrumentRef.status)) { //$NON-NLS-1$
				// change the status to U
				decodedInstrumentRef.status = "U"; //$NON-NLS-1$

			} else if ("D".equals(decodedInstrumentRef.status)) { //$NON-NLS-1$
				decodedInstrumentRef.status = "U"; //$NON-NLS-1$

			} else if ("N".equals(decodedInstrumentRef.status)) { //$NON-NLS-1$
				// unchanged status
			} else if ("U".equals(decodedInstrumentRef.status)) { //$NON-NLS-1$
				// remain unchanged
			} else {
				throw new Exception("unsupported Status"); //$NON-NLS-1$
			}

			logger.debug("new status of the instrument :" //$NON-NLS-1$
					+ decodedInstrumentRef.status);

			decodedInstrumentRef.name = StringTools
					.convertToPhysicalName(instrument.getName());

			String newStreamName = constructStreamName(decodedInstrumentRef);

			logger.debug("saving the stream ..."); //$NON-NLS-1$
			saveInstrumentStream(sei, newStreamName);
			logger.debug("stream saved"); //$NON-NLS-1$

			if (!newStreamName.equals(decodedInstrumentRef.fullStreamName)) {
				logger.debug("removing the old stream :" //$NON-NLS-1$
						+ decodedInstrumentRef.fullStreamName);
				streamStorage.deleteStream(StringTools.removeExtension(
						decodedInstrumentRef.fullStreamName,
						SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE),
						SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);
			}

			logger.debug("done"); //$NON-NLS-1$

		} else {

			// nouvel instrument ...
			DecodedInstrumentRef r = new DecodedInstrumentRef();
			r.name = instrument.getName();

			String constructStreamName = constructStreamName(r);

			logger.debug("stream name :" + constructStreamName); //$NON-NLS-1$

			saveInstrumentStream(instrument, constructStreamName);

		}

		fireInstrumentsChanged();

	}

	public void deleteEditableInstrument(String instrumentReference)
			throws Exception {

		HashMap<String, DecodedInstrumentRef> decoded = readDecodedStreamReferences();

		if (!decoded.containsKey(instrumentReference))
			throw new Exception("unknown instrument reference"); //$NON-NLS-1$

		DecodedInstrumentRef decodedInstrumentRef = decoded
				.get(instrumentReference);

		if ("N".equals(decodedInstrumentRef.status)) { //$NON-NLS-1$

			logger.debug("deleting the newly created instrument"); //$NON-NLS-1$

			streamStorage.deleteStream(StringTools.removeExtension(
					decodedInstrumentRef.fullStreamName,
					SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE),
					SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);

		} else {

			// change the status to deleted ...

			String oldStreamName = decodedInstrumentRef.fullStreamName;

			decodedInstrumentRef.status = "D"; //$NON-NLS-1$
			String newStreamName = constructStreamName(decodedInstrumentRef);

			streamStorage.saveStream(StringTools.removeExtension(newStreamName,
					SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE),
					SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE, streamStorage
							.openStream(oldStreamName));

			streamStorage.deleteStream(StringTools.removeExtension(
					oldStreamName, SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE),
					SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);

		}

		fireInstrumentsChanged();

	}

	public void dispose() {
		if (streamStorage instanceof Disposable)
			((Disposable) streamStorage).dispose();
	}

	private void saveInstrumentStream(IEditableInstrument instrument,
			String streamName) throws Exception {
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
				streamStorage.saveStream(StringTools.removeExtension(
						streamName, SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE),
						SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE, fileInputStream);
			} finally {
				fileInputStream.close();
			}
		} finally {
			tmpFile.delete();
		}
	}

	public String getEditableInstrumentDigest(String name) throws Exception {

		HashMap<String, DecodedInstrumentRef> dr = readDecodedStreamReferences();
		DecodedInstrumentRef decodedInstrumentRef = dr.get(name);
		if (decodedInstrumentRef == null)
			throw new Exception("stream " + name + " not found"); //$NON-NLS-1$ //$NON-NLS-2$

		return streamStorage
				.getStreamDigest(decodedInstrumentRef.fullStreamName);
	}

	public Map<String, String> getAllEditableInstrumentDigests()
			throws Exception {

		HashMap<String, DecodedInstrumentRef> dr = readDecodedStreamReferences();

		HashMap<String, String> retvalue = new HashMap<String, String>();

		for (Iterator<Entry<String, DecodedInstrumentRef>> iterator = dr
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, DecodedInstrumentRef> e = (Entry<String, DecodedInstrumentRef>) iterator
					.next();

			retvalue.put(e.getKey(),
					streamStorage.getStreamDigest(e.getValue().fullStreamName));
		}

		return retvalue;
	}

	// Options for saving direct versioned elements to the repository

	public void saveInstrument(DecodedInstrumentRef r, InputStream is)
			throws IOException {
		streamStorage
				.saveStream(
						getFullStreamName(r),
						VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE,
						is);
	}

	public void deleteInstrument(DecodedInstrumentRef r) throws Exception {
		streamStorage
				.deleteStream(
						getFullStreamName(r),
						VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);
	}

	public InputStream openInstrumentStream(DecodedInstrumentRef r)
			throws Exception {
		return streamStorage.openStream(getFullStreamName(r));
	}

	public String getFullStreamName(DecodedInstrumentRef r) {
		return r.status + "_" //$NON-NLS-1$
				+ r.instrumentID + "_" //$NON-NLS-1$
				+ r.version + "_" //$NON-NLS-1$
				+ StringTools.convertToPhysicalName(r.name);

	}

	public String getFullStreamNameWithExtension(DecodedInstrumentRef r) {
		return getFullStreamName(r) + "."
				+ SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE;
	}

}
