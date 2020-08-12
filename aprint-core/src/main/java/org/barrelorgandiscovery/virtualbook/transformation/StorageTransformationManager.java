package org.barrelorgandiscovery.virtualbook.transformation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.tools.IniFileParser;
import org.barrelorgandiscovery.tools.NotImplementedException;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;


public class StorageTransformationManager implements TransformationManager {

	private static final String IMPORTERSCRIPT_TYPE = "importerscript"; //$NON-NLS-1$

	private static final String TRANSPOSITION_TYPE = "transposition"; //$NON-NLS-1$

	private static final Logger logger = Logger
			.getLogger(StorageTransformationManager.class);

	/**
	 * La liste des transpositions
	 */
	private ArrayList<AbstractTransposeVirtualBook> transpositions = new ArrayList<AbstractTransposeVirtualBook>();

	/**
	 * Importer midi ...
	 */
	private ArrayList<AbstractMidiImporter> midiimporters = new ArrayList<AbstractMidiImporter>();

	/**
	 * stream storage connection
	 */
	private StreamStorage streamstorage;

	/**
	 * Constructor
	 * 
	 * @param reptransposition
	 *            folder conatining the ".transposition" files
	 * @param gm
	 *            the scalemanager
	 */
	public StorageTransformationManager(StreamStorage reptransposition,
			ScaleManager gm) {

		logger.debug("Reading Transpositions"); //$NON-NLS-1$

		// Lecture des transposition contenues dans le répertoire
		String[] listefichierstransposition = reptransposition
				.listStreams(TRANSPOSITION_TYPE);

		logger.debug("liste des transposition trouvées :" //$NON-NLS-1$
				+ listefichierstransposition);

		// parcours des fichiers et lecture des transpositions

		for (int i = 0; i < listefichierstransposition.length; i++) {
			String f = listefichierstransposition[i];
			logger.debug("reading " + f); //$NON-NLS-1$
			try {

				LinearTranspositionParser tp = new LinearTranspositionParser(gm);
				IniFileParser p = new IniFileParser(new InputStreamReader(
						reptransposition.openStream(f)), tp);

				p.parse();

				transpositions.add(tp.getTransposition());

				logger.info("Transposition " + tp.getTransposition().getName() //$NON-NLS-1$
						+ " sucessfully read"); //$NON-NLS-1$

			} catch (Exception ex) {
				logger.error("error while loading transposition " + f, ex); //$NON-NLS-1$
			}
		}

		// Ajout de la transposition midi
		logger.debug("adding midi transposition"); //$NON-NLS-1$
		try {
			transpositions.add(LinearTransposition.getMidiTransposition());
		} catch (TranspositionException ex) {
			logger.error("error while adding midi transposition", ex); //$NON-NLS-1$
		}

		// adding a default midi translation

		logger
				.debug("adding a default translation with midi to every scales ...");

		String[] scalesnames = gm.getScaleNames();
		for (int i = 0; i < scalesnames.length; i++) {

			String currentscalename = scalesnames[i];
			Scale currentscale = gm.getScale(currentscalename);

			try {
				LinearTransposition defaulttransposition = TranspositionIO
						.createDefaultMidiTransposition(currentscale);
				transpositions.add(defaulttransposition);
			} catch (Exception ex) {
				logger.error("creating default transposition", ex);
			}
		}

		logger.debug("reading importers"); //$NON-NLS-1$

		String[] listemidiimporter = reptransposition
				.listStreams(IMPORTERSCRIPT_TYPE);
		for (int i = 0; i < listemidiimporter.length; i++) {
			String importerscriptfilename = listemidiimporter[i];
			logger.debug("reading script :" + importerscriptfilename); //$NON-NLS-1$
			try {

				ScriptMidiImporter importer = new ScriptMidiImporter(
						importerscriptfilename, reptransposition
								.openStream(importerscriptfilename), gm);

				midiimporters.add(importer);
				logger.debug("midi importer " + importerscriptfilename //$NON-NLS-1$
						+ " sucessfully read"); //$NON-NLS-1$

			} catch (Exception ex) {
				logger.error("error while loading script " //$NON-NLS-1$
						+ importerscriptfilename, ex);
			}
		}

		this.streamstorage = reptransposition;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.cartonvirtuel.TranspositionGestionnaire#findTransposition(fr.freydierepatrice.gamme.Gamme,
	 *      fr.freydierepatrice.gamme.Gamme)
	 */
	public ArrayList<AbstractTransformation> findTransposition(Scale source,
			Scale destination) {

		logger.debug("seek transpositions between " + source.getName() //$NON-NLS-1$
				+ " and " + destination.getName()); //$NON-NLS-1$

		ArrayList<AbstractTransformation> t = new ArrayList<AbstractTransformation>();
		for (int i = 0; i < transpositions.size(); i++) {

			if (transpositions.get(i) instanceof AbstractTransposeVirtualBook) {
				AbstractTransposeVirtualBook tr = (AbstractTransposeVirtualBook) transpositions
						.get(i);
				if (tr.getScaleSource().equals(source)
						&& tr.getScaleDestination().equals(destination))
					t.add(tr);
			}
		}

		logger.debug("Search find " + t.size() + " elements "); //$NON-NLS-1$ //$NON-NLS-2$

		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.cartonvirtuel.TransformationManager#findImporter(fr.freydierepatrice.gamme.Gamme)
	 */
	public ArrayList<AbstractMidiImporter> findImporter(Scale destination) {

		assert destination != null;

		ArrayList<AbstractMidiImporter> retvalue = new ArrayList<AbstractMidiImporter>();
		for (AbstractMidiImporter i : midiimporters) {
			if (i.getScaleDestination().equals(destination)) {
				retvalue.add(i);
			}
		}

		return retvalue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.virtualbook.transformation.TransformationManager#saveImporter(fr.freydierepatrice.virtualbook.transformation.AbstractMidiImporter)
	 */
	public void saveImporter(AbstractMidiImporter importer) throws Exception {
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.virtualbook.transformation.TransformationManager#deleteImporter(fr.freydierepatrice.virtualbook.transformation.AbstractMidiImporter)
	 */
	public void deleteImporter(AbstractMidiImporter importer) throws Exception {

		if (importer == null)
			throw new InvalidParameterException(Messages
					.getString("StorageTransformationManager.19")); //$NON-NLS-1$

		streamstorage.deleteStream(StringTools.convertToPhysicalNameWithEndingHashCode(importer
				.getName()), IMPORTERSCRIPT_TYPE);

		// if success remove the importer ...

		this.midiimporters.remove(importer);

		logger.debug("importer " + importer.getName() + " removed"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.virtualbook.transformation.TransformationManager#saveTransformation(fr.freydierepatrice.virtualbook.transformation.AbstractTransformation)
	 */
	public void saveTransformation(AbstractTransformation transformation)
			throws Exception {

		assert transformation != null;

		if (transformation instanceof LinearTransposition) {

			LinearTransposition linearTransposition = (LinearTransposition) transformation;
			if (!linearTransposition.isBuiltin()) // no saving for the builtin
			// translations
			{

				String transname = StringTools
						.convertToPhysicalNameWithEndingHashCode(transformation.getName());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				TranspositionIO.writeLinearTransposition(linearTransposition,
						baos);

				streamstorage.saveStream(transname, TRANSPOSITION_TYPE,
						new ByteArrayInputStream(baos.toByteArray()));
			}
			// ok no errors in saving ...

			// remove the transformation and read it

			int i = 0;
			while (i < transpositions.size()) {
				AbstractTransformation currenttransposition = (AbstractTransformation) transpositions
						.get(i);
				if (currenttransposition.getName().equals(
						transformation.getName())) {
					transpositions.remove(currenttransposition);
				} else {
					i++;
				}
			}

			transpositions.add((AbstractTransposeVirtualBook) transformation);

			return;
		}

		throw new Exception(
				Messages.getString("StorageTransformationManager.22") + transformation.getClass() //$NON-NLS-1$
						+ Messages.getString("StorageTransformationManager.23")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.virtualbook.transformation.TransformationManager#deleteTransformation(fr.freydierepatrice.virtualbook.transformation.AbstractTransformation)
	 */
	public void deleteTransformation(AbstractTransformation transformation)
			throws Exception {
		if (transformation == null)
			throw new InvalidParameterException(Messages
					.getString("StorageTransformationManager.24")); //$NON-NLS-1$

		streamstorage.deleteStream(StringTools
				.convertToPhysicalNameWithEndingHashCode(transformation.getName()),
				TRANSPOSITION_TYPE);

		// if success remove the importer ...

		this.transpositions.remove(transformation);

		logger.debug("importer " + transformation.getName() + " removed"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
