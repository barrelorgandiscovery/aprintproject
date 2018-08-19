package org.barrelorgandiscovery.instrument;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;


/**
 * Instrument manager managed in a folder, ZIP, an abstraction is used there to
 * manage streams
 * 
 * 
 * @author Freydiere Patrice
 * 
 */
public class StorageInstrumentManager implements InstrumentManager {

	private static final String INSTRUMENT_TYPE = "instrument"; //$NON-NLS-1$

	/**
	 * Logger
	 */
	private static final Logger logger = Logger
			.getLogger(StorageInstrumentManager.class);

	/**
	 * Mémorisation des différents instruments ..
	 */
	private List<Instrument> instruments = new ArrayList<Instrument>();

	private StreamStorage ss;

	/**
	 * Constructor of the instrument manager
	 * 
	 * @param folder
	 *            folder containine the instruments
	 * @param gm
	 *            scale manager associated to the instruments
	 * 
	 * @deprecated use the other constructor instead
	 */
	public StorageInstrumentManager(File folder, ScaleManager gm) throws Exception{
		this(new FolderStreamStorage(folder), gm);
	}

	/**
	 * Constructor using a stream storage
	 * 
	 * @param fis
	 * @param gm
	 */
	public StorageInstrumentManager(StreamStorage fis, ScaleManager gm) {

		logger.debug("Reading the instrument definition"); //$NON-NLS-1$
		// loading the Instruments ...

		String[] instrumentstreams = fis.listStreams(INSTRUMENT_TYPE);

		logger.debug("instrument number ... " + instrumentstreams.length); //$NON-NLS-1$

		for (int i = 0; i < instrumentstreams.length; i++) {
			try {
				String current = instrumentstreams[i];
				logger.debug("read the instrument .. " + current); //$NON-NLS-1$

				Instrument ins = InstrumentIO.readInstrument(gm, fis, current);
				instruments.add(ins);

				logger.debug("Instrument " + current + " successfully read"); //$NON-NLS-1$ //$NON-NLS-2$

			} catch (Exception ex) {
				logger.error("FileInstrumentManager", ex); //$NON-NLS-1$
			}
		}

		this.ss = fis;

		logger.debug("Reading the instrument definition done"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.instrument.InstrumentManager#getInstrument(java.lang.String)
	 */
	public Instrument getInstrument(String name) {

		for (int i = 0; i < instruments.size(); i++) {
			Instrument ins = instruments.get(i);
			if (ins.getName().equals(name))
				return ins;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.instrument.InstrumentManager#getInstrument(fr.freydierepatrice.scale.Scale)
	 */
	public Instrument[] getInstrument(Scale gamme) {

		List<Instrument> g = new ArrayList<Instrument>();

		for (int i = 0; i < instruments.size(); i++) {
			Instrument ins = instruments.get(i);
			if (ins.getScale().equals(gamme))
				g.add(ins);
		}

		return g.toArray(new Instrument[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.instrument.InstrumentManager#listInstruments()
	 */
	public Instrument[] listInstruments() {
		return instruments.toArray(new Instrument[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.instrument.InstrumentManager#deleteInstrument(fr.freydierepatrice.instrument.Instrument)
	 */
	public void deleteInstrument(Instrument instrument) throws Exception {

		logger.debug("deleteInstrument " + instrument.getName());
		String streamname = StringTools.convertToPhysicalNameWithEndingHashCode(instrument
				.getName());

		ss.deleteStream(streamname, INSTRUMENT_TYPE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.instrument.InstrumentManager#saveInstrument(fr.freydierepatrice.instrument.Instrument)
	 */
	public void saveInstrument(Instrument instrument) throws Exception {

		logger.debug("saveInstrument" + instrument.getName());
		String streamname = StringTools.convertToPhysicalNameWithEndingHashCode(instrument
				.getName());

		InstrumentIO.writeInstrument(ss, instrument, streamname);

	}

}
