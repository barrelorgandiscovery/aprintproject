package org.barrelorgandiscovery.editableinstrument;

import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript.InstrumentScriptLanguage;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript.InstrumentScriptType;
import org.barrelorgandiscovery.gui.ainstrument.SBCreator;
import org.barrelorgandiscovery.gui.ainstrument.SBCreator.SF2SoundBankResult;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.instrument.RegisterSoundLink;
import org.barrelorgandiscovery.instrument.SampleMapping;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.streamstorage.IStreamRef;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;
import org.barrelorgandiscovery.virtualbook.transformation.GroovyImporterScript;
import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;
import org.barrelorgandiscovery.virtualbook.transformation.TranspositionIO;

import gervill.SF2Soundbank;

/**
 * This class is a bridge between an editable instrument Manager and a
 * repository 2
 * 
 * @author Freydiere Patrice
 * 
 */
public class EditableInstrumentManagerRepository2Adapter implements
		Repository2, Disposable, EditableInstrumentManagerRepository {

	private static Logger logger = Logger
			.getLogger(EditableInstrumentManagerRepository2Adapter.class);

	private EditableInstrumentManager manager = null;

	private File cacheFolder = null;

	private final String INSTRUMENT_CACHE_VERSION = "V1";

	/**
	 * Name of the repository ...
	 */
	private String name;
	
	/**
	 * label,may be null
	 */
	private String label;

	public EditableInstrumentManagerRepository2Adapter(
			EditableInstrumentManager manager, String name,String label, File cacheFolder) {
		assert manager != null;
		assert name != null;
		this.manager = manager;
		this.label = label;

		if (cacheFolder != null) {
			if (cacheFolder.exists()) {
				this.cacheFolder = cacheFolder;

			} else {
				if (cacheFolder.mkdirs()) {
					this.cacheFolder = cacheFolder;
				}
			}
		}

		if (logger.isDebugEnabled())
			logger.debug("cache enabled");

		manager.addListener(new EditableInstrumentManagerListener() {

			public void instrumentsChanged() {
				try {

					reloadEditableInstruments();

					for (Iterator iterator = listeners.iterator(); iterator
							.hasNext();) {
						RepositoryChangedListener r = (RepositoryChangedListener) iterator
								.next();

						r.instrumentsChanged();
						r.scalesChanged();
						r.transformationAndImporterChanged();

					}
				} catch (Exception ex) {
					logger.debug("error in manging the events", ex); //$NON-NLS-1$
				}
			}
		});

		this.name = name;

		reloadEditableInstruments();
	}

	private ArrayList<Instrument> readedInstruments = new ArrayList<Instrument>();

	private ArrayList<AbstractTransformation> transformations = new ArrayList<AbstractTransformation>();

	private ArrayList<AbstractMidiImporter> midiImporters = new ArrayList<AbstractMidiImporter>();

	private static class TemporaryFileStreamRef implements IStreamRef,
			Disposable {

		private File TempFile = null;

		public TemporaryFileStreamRef(File refTempFile) throws Exception {
			this.TempFile = refTempFile;
		}

		public InputStream open() throws IOException {
			logger.debug("opening the stream ..."); //$NON-NLS-1$
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamsTools.copyStream(new FileInputStream(TempFile), baos);
			return new ByteArrayInputStream(baos.toByteArray());
		}

		public void dispose() {
			if (TempFile != null) {
				TempFile.delete();
				TempFile = null;
			}
		}

		@Override
		protected void finalize() throws Throwable {
			dispose();
		}

	}

	private static class CacheInstrumentBag implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 929404074305736768L;

		public byte[] bi = null;
		public String name = null;
		public String description = null;
		public Scale scale = null;
		public RegisterSoundLink def = null;
		public InstrumentScript[] scripts = null;
	}

	private Map<String, String> instrumentToEditableInstrumentNameMapping = new HashMap<String, String>();

	/**
	 * Load the editable instruments as instruments
	 */
	public void reloadEditableInstruments() {
		try {

			freeInstruments();
			transformations.clear();
			instrumentToEditableInstrumentNameMapping.clear();
			String[] listEditableInstruments = manager
					.listEditableInstruments();

			Map<String, String> digestCache = null;

			if (cacheFolder != null) {
				try {
					digestCache = manager.getAllEditableInstrumentDigests();
				} catch (Throwable t) {
					logger.warn("cannot create digest cache ...", t);
				}
			}

			for (int i = 0; i < listEditableInstruments.length; i++) {
				String currentInstrument = listEditableInstruments[i];

				try {
					logger.debug("loading editable instrument :" //$NON-NLS-1$
							+ currentInstrument);

					logger.debug("try to load the cache");
					if (cacheFolder != null) {

						IStreamRef sr = null;

						String digest = (digestCache != null ? digestCache
								.get(currentInstrument) : manager
								.getEditableInstrumentDigest(currentInstrument));

						String cacheFilePrefix = StringTools
								.convertToPhysicalName(""
										+ INSTRUMENT_CACHE_VERSION
										+ currentInstrument + digest);

						try {
							final File sbfile = new File(cacheFolder,
									cacheFilePrefix + ".sf2");
							if (sbfile.exists()) {
								sr = new IStreamRef() {
									public InputStream open()
											throws IOException {
										return new BufferedInputStream(
												new FileInputStream(sbfile));
									}
								};

								// reading the scale definition and name ...

								ObjectInputStream ois = new ObjectInputStream(
										new FileInputStream(new File(
												cacheFolder, cacheFilePrefix
														+ ".os")));
								try {
									CacheInstrumentBag ib = (CacheInstrumentBag) ois
											.readObject();

									Instrument ins = new Instrument(ib.name,
											ib.scale, sr,
											ImageTools.loadImage(Toolkit
													.getDefaultToolkit()
													.createImage(ib.bi)),
											ib.description);

									instrumentToEditableInstrumentNameMapping
											.put(ib.name, currentInstrument);

									if (ib.def != null) {
										List<String> pipeStopGroupNamesInWhichThereAreMappings = ib.def
												.getPipeStopGroupNamesInWhichThereAreMappings();
										for (Iterator iterator = pipeStopGroupNamesInWhichThereAreMappings
												.iterator(); iterator.hasNext();) {
											String s = (String) iterator.next();
											List<String> pipeStopNamesInWhichThereAreMappings = ib.def
													.getPipeStopNamesInWhichThereAreMappings(s);
											for (Iterator iterator2 = pipeStopNamesInWhichThereAreMappings
													.iterator(); iterator2
													.hasNext();) {
												String s2 = (String) iterator2
														.next();

												ins.getRegisterSoundLink()
														.defineLink(
																s,
																s2,
																ib.def.getInstrumentNumber(
																		s, s2));
											}
										}

										if (ib.def.getDrumSoundBank() != -1) {
											ins.getRegisterSoundLink()
													.setDrumSoundBank(
															ib.def.getDrumSoundBank());
										}
									}

									readedInstruments.add(ins);

									LinearTransposition t = TranspositionIO
											.createDefaultMidiTransposition(ins
													.getScale());

									transformations.add(t);

									// adding scripts ....
									InstrumentScript[] importerScripts = ib.scripts;

									for (int j = 0; j < importerScripts.length; j++) {
										InstrumentScript instrumentScript = importerScripts[j];

										if (instrumentScript.getType() != InstrumentScriptType.IMPORTER_SCRIPT)
											continue;

										if (instrumentScript
												.getScriptLanguage() != InstrumentScriptLanguage.GROOVY) {
											logger.warn("script "
													+ instrumentScript
															.getScriptLanguage()
													+ " not supported, skip it");
											continue;
										}

										GroovyImporterScript gis = new GroovyImporterScript(
												instrumentScript,
												ins.getScale());

										midiImporters.add(gis);
										logger.debug("script transformation "
												+ instrumentScript + " added ");

									}

									logger.debug("cache successfully read for instrument "
											+ currentInstrument);

									continue; //

								} finally {
									ois.close();
								}

							}

						} catch (Throwable ex) {
							logger.warn("cache not valid for "
									+ currentInstrument);
						}

					}

					IEditableInstrument loadEditableInstrument = manager
							.loadEditableInstrument(currentInstrument);
					try {

						logger.debug("creating default sound bank ...."); //$NON-NLS-1$

						final SBCreator sb = new SBCreator();

						String currentPipeStopGroup = EditableInstrument.DEFAULT_PIPESTOPGROUPNAME;

						List<SoundSample> soundSampleList = loadEditableInstrument
								.getSoundSampleList(currentPipeStopGroup);

						ArrayList<SampleMapping> a = new ArrayList<SampleMapping>();
						for (Iterator iterator = soundSampleList.iterator(); iterator
								.hasNext();) {
							SoundSample sampleMapping = (SoundSample) iterator
									.next();

							SampleMapping sampleMapping2 = loadEditableInstrument
									.getSampleMapping(currentPipeStopGroup,
											sampleMapping);
							a.add(sampleMapping2);
						}

						SF2SoundBankResult createSoundBankResult = sb
								.createSoundBank(loadEditableInstrument);
						SF2Soundbank soundBank = createSoundBankResult.soundBank;

						//
						//
						// SF2Soundbank soundBank =
						// sb.createSimpleSoundBank(a
						// .toArray(new SampleMapping[0]));

						instrumentToEditableInstrumentNameMapping.put(
								loadEditableInstrument.getName(),
								currentInstrument);

						if (cacheFolder != null) {
							try {

								String digest = (digestCache != null ? digestCache
										.get(currentInstrument)
										: manager
												.getEditableInstrumentDigest(currentInstrument));

								String cacheFilePrefix = StringTools
										.convertToPhysicalName(""
												+ INSTRUMENT_CACHE_VERSION
												+ currentInstrument + digest);

								File sbfile = new File(cacheFolder,
										cacheFilePrefix + ".sf2");

								soundBank.save(sbfile);

							} catch (Throwable t) {
								logger.warn("can't save sb for instrument "
										+ currentInstrument, t);
							}
						}

						File TempFile = File.createTempFile("tmp", "sf2"); //$NON-NLS-1$ //$NON-NLS-2$
						soundBank.save(TempFile);

						IStreamRef ref = new TemporaryFileStreamRef(TempFile);

						Instrument ins = new Instrument(
								loadEditableInstrument.getName(),
								loadEditableInstrument.getScale(), ref,
								loadEditableInstrument.getInstrumentPicture(),
								loadEditableInstrument
										.getInstrumentDescription());

						// adding mapping

						RegisterSoundLink rsl = ins.getRegisterSoundLink();

						HashMap<String, Integer> mapping = createSoundBankResult.soundbankMapping;
						for (Iterator iterator = mapping.entrySet().iterator(); iterator
								.hasNext();) {
							Entry<String, Integer> e = (Entry<String, Integer>) iterator
									.next();

							String[] r = e.getKey().split("-");
							if (r.length == 2) {
								rsl.defineLink(r[0], r[1], e.getValue());
							}
						}

						if (createSoundBankResult.percussionDrum != null)
							rsl.setDrumSoundBank(createSoundBankResult.percussionDrum);

						if (cacheFolder != null) {
							try {

								String digest = (digestCache != null ? digestCache
										.get(currentInstrument)
										: manager
												.getEditableInstrumentDigest(currentInstrument));

								String cacheFilePrefix = StringTools
										.convertToPhysicalName(""
												+ INSTRUMENT_CACHE_VERSION
												+ currentInstrument + digest);

								ObjectOutputStream ois = new ObjectOutputStream(
										new FileOutputStream(new File(
												cacheFolder, cacheFilePrefix
														+ ".os")));

								CacheInstrumentBag ib = new CacheInstrumentBag();
								ib.name = ins.getName();
								ib.description = ins.getDescriptionUrl();
								ib.scale = ins.getScale();
								ib.def = ins.getRegisterSoundLink();
								ib.scripts = loadEditableInstrument
										.getScripts();

								ByteArrayOutputStream imagebaos = new ByteArrayOutputStream();
								ImageIO.write(ImageTools.loadImage(ins
										.getThumbnail()), "PNG", imagebaos);

								ib.bi = imagebaos.toByteArray();

								ois.writeObject(ib);

								ois.close();

							} catch (Throwable t) {
								logger.warn(
										"can't save ib cache for instrument "
												+ currentInstrument, t);
							}
						}

						readedInstruments.add(ins);

						logger.debug("adding transformations  ...");

						LinearTransposition t = TranspositionIO
								.createDefaultMidiTransposition(ins.getScale());

						transformations.add(t);
						logger.debug("default midi transformation added");

						InstrumentScript[] importerScripts = loadEditableInstrument
								.getScripts();

						for (int j = 0; j < importerScripts.length; j++) {
							InstrumentScript instrumentScript = importerScripts[j];
							if (instrumentScript.getType() != InstrumentScriptType.IMPORTER_SCRIPT)
								continue;

							if (instrumentScript.getScriptLanguage() != InstrumentScriptLanguage.GROOVY) {
								logger.warn("script "
										+ instrumentScript.getScriptLanguage()
										+ " not supported, skip it");
								continue;
							}

							GroovyImporterScript gis = new GroovyImporterScript(
									instrumentScript, ins.getScale());

							midiImporters.add(gis);
							logger.debug("script transformation "
									+ instrumentScript + " added ");

						}

						logger.debug("instrument " + currentInstrument + " loaded"); //$NON-NLS-1$ //$NON-NLS-2$

					} finally {
						logger.debug("dispose instrument ..."); //$NON-NLS-1$
						loadEditableInstrument.dispose();
					}

				} catch (Exception ex) {
					logger.error("fail to load editable instrument :" //$NON-NLS-1$
							+ currentInstrument, ex);
				}
			}

		} catch (Exception ex) {
			logger.error("error in loadEditableInstruments", ex); //$NON-NLS-1$
		}
	}

	private void freeInstruments() {
		logger.debug("freeInstruments"); //$NON-NLS-1$
		for (Iterator iterator = readedInstruments.iterator(); iterator
				.hasNext();) {
			Instrument ins = (Instrument) iterator.next();
			if (ins instanceof Disposable) {
				((Disposable) ins).dispose();
			}
		}

		readedInstruments.clear();
	}

	private Vector<RepositoryChangedListener> listeners = new Vector<RepositoryChangedListener>();

	public void addRepositoryChangedListener(RepositoryChangedListener listener) {
		if (listener == null)
			return;
		listeners.add(listener);
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public String getLabel() {
		if (label != null && !label.isEmpty())
			return label;
		return name;
	}

	/**
	 * this repository is user ReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}

	public void removeRepositoryChangedListener(
			RepositoryChangedListener listener) {
		if (listener == null)
			return;
		listeners.remove(listener);
	}

	public void deleteInstrument(Instrument instrument) throws Exception {
		throw new UnsupportedOperationException();
	}

	public Instrument getInstrument(String name) {
		for (Iterator iterator = readedInstruments.iterator(); iterator
				.hasNext();) {
			Instrument currentInstrument = (Instrument) iterator.next();
			if (currentInstrument.getName().equals(name))
				return currentInstrument;
		}
		return null;
	}

	public Instrument[] getInstrument(Scale scale) {
		for (Iterator iterator = readedInstruments.iterator(); iterator
				.hasNext();) {
			Instrument currentInstrument = (Instrument) iterator.next();
			if (currentInstrument.getScale() == scale)
				return new Instrument[] { currentInstrument };
		}

		return new Instrument[0];
	}

	public Instrument[] listInstruments() {
		return readedInstruments.toArray(new Instrument[0]);
	}

	public void saveInstrument(Instrument instrument) throws Exception {
		throw new UnsupportedOperationException();
	}

	public void deleteScale(Scale scale) throws Exception {
		throw new UnsupportedOperationException();
	}

	public Scale getScale(String name) {
		for (Iterator iterator = readedInstruments.iterator(); iterator
				.hasNext();) {
			Instrument instrument = (Instrument) iterator.next();
			if (instrument.getScale().getName().equals(name))
				return instrument.getScale();
		}
		return null;
	}

	public String[] getScaleNames() {

		ArrayList<String> retvalue = new ArrayList<String>();

		for (Iterator iterator = readedInstruments.iterator(); iterator
				.hasNext();) {
			Instrument instrument = (Instrument) iterator.next();
			retvalue.add(instrument.getScale().getName());
		}

		return retvalue.toArray(new String[0]);
	}

	public void saveScale(Scale scale) throws Exception {
		throw new UnsupportedOperationException();
	}

	public void deleteImporter(AbstractMidiImporter importer) throws Exception {
		throw new UnsupportedOperationException();

	}

	public void deleteTransformation(AbstractTransformation transformation)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	public ArrayList<AbstractMidiImporter> findImporter(Scale destination) {

		logger.debug("looking for importer on scale :" + destination);

		ArrayList<AbstractMidiImporter> retvalue = new ArrayList<AbstractMidiImporter>();

		for (Iterator iterator = midiImporters.iterator(); iterator.hasNext();) {
			Object t = iterator.next();

			AbstractMidiImporter abstractTransformation = (AbstractMidiImporter) t;

			if (abstractTransformation.getScaleDestination() == destination) {
				logger.debug("adding " + abstractTransformation);
				retvalue.add(abstractTransformation);
			}

		}

		if (logger.isDebugEnabled()) {
			for (Iterator iterator = retvalue.iterator(); iterator.hasNext();) {
				AbstractMidiImporter abstractMidiImporter = (AbstractMidiImporter) iterator
						.next();
				logger.debug("importer found :" + abstractMidiImporter);
			}
		}

		return retvalue;

	}

	public ArrayList<AbstractTransformation> findTransposition(Scale source,
			Scale destination) {
		ArrayList<AbstractTransformation> retvalue = new ArrayList<AbstractTransformation>();

		for (Iterator iterator = transformations.iterator(); iterator.hasNext();) {

			AbstractTransformation abstractTransformation = (AbstractTransformation) iterator
					.next();

			if (abstractTransformation.getScaleDestination() == destination)
				retvalue.add(abstractTransformation);
		}

		return retvalue;
	}

	public void saveImporter(AbstractMidiImporter importer) throws Exception {
		throw new UnsupportedOperationException();
	}

	public void saveTransformation(AbstractTransformation transformation)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.editableinstrument.EditableInstrumentManagerRepository
	 * #getEditableInstrumentManager()
	 */
	public EditableInstrumentManager getEditableInstrumentManager() {
		return manager;
	}

	public String findAssociatedEditableInstrument(String instrumentName) {
		return instrumentToEditableInstrumentNameMapping.get(instrumentName);
	}

	public String findAssociatedEditableInstrumentName(String instrumentname) {
		return instrumentToEditableInstrumentNameMapping.get(instrumentname);
	}

	public void dispose() {
		freeInstruments();
	}

	public void signalInstrumentChanged() {
		logger.debug("repository changed");

		RepositoryChangedListener[] lstr = listeners
				.toArray(new RepositoryChangedListener[listeners.size()]);

		for (RepositoryChangedListener rcl : lstr) {

			try {
				rcl.instrumentsChanged();
			} catch (Throwable t) {
				logger.error(
						"error in signaling repository changed :"
								+ t.getMessage(), t);
			}

		}
	}

}
