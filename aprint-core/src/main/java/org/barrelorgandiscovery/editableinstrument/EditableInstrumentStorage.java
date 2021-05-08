package org.barrelorgandiscovery.editableinstrument;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat.Type;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.SampleMapping;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.tools.streamstorage.ZipStreamMarshaller;

/**
 * Instrument Storage, for saving and loading instruments ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class EditableInstrumentStorage {

	private static final String DRUMWAV = "drumwav";
	private static final String GROOVYIMPORTERSCRIPTSEXTENSION = "groovyimporterscripts";
	private static final String GROOVYEXPORTERSCRIPTSEXTENSION = "groovyexporterscripts";
	private static Logger logger = Logger
			.getLogger(EditableInstrumentStorage.class);

	public void save(IEditableInstrument model, OutputStream os)
			throws Exception {

		logger.debug("saving instrument"); //$NON-NLS-1$

		File createTempFile = File.createTempFile("storagefoldertemp", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		createTempFile.delete();
		createTempFile.mkdirs();
		try {

			FolderStreamStorage fss = new FolderStreamStorage(createTempFile);

			Scale scale = model.getScale();

			ByteArrayOutputStream scaleStream = new ByteArrayOutputStream();
			ScaleIO.writeGamme(scale, scaleStream);
			scaleStream.close();

			logger.debug("saving the scale ..."); //$NON-NLS-1$

			fss.saveStream(StringTools.convertToPhysicalName(scale.getName()),
					"scale", //$NON-NLS-1$
					new ByteArrayInputStream(scaleStream.toByteArray()));

			String[] pipeStopGroups = model.getPipeStopGroupsAndRegisterName();
			for (int i = 0; i < pipeStopGroups.length; i++) {
				String pipeStopGroup = pipeStopGroups[i];

				// saving the elements associated to the pipestops ...
				List<SoundSample> soundSampleList = model
						.getSoundSampleList(pipeStopGroup);

				logger.debug("saving sound sample list for " + pipeStopGroup); //$NON-NLS-1$

				if (soundSampleList != null) {

					for (Iterator iterator = soundSampleList.iterator(); iterator
							.hasNext();) {
						SoundSample soundSample = (SoundSample) iterator.next();

						// save the sound sample ....
						SampleMapping sampleMapping = model.getSampleMapping(
								pipeStopGroup, soundSample);

						if (sampleMapping != null) {
							logger.debug("saving the sound mapping ... " //$NON-NLS-1$
									+ sampleMapping);

							SoundSample ss = sampleMapping.getSoundSample();

							// pipeStopGroup -> Register, or default if not

							String keyname = pipeStopGroup + "_" //$NON-NLS-1$
									+ sampleMapping.getFirstMidiCode() + "_" //$NON-NLS-1$
									+ sampleMapping.getLastMidiCode() + "_" //$NON-NLS-1$
									+ StringTools.toHex(ss.getName()) + "_" //$NON-NLS-1$
									+ ss.getLoopStart() + "_" + ss.getLoopEnd() //$NON-NLS-1$
									+ "_" + ss.getMidiRootNote(); //$NON-NLS-1$

							logger.debug("stream key name " + keyname); //$NON-NLS-1$

							ByteArrayOutputStream audioStream = new ByteArrayOutputStream();

							ManagedAudioInputStream managedAudioInputStream = ss
									.getManagedAudioInputStream();
							AudioSystem.write(managedAudioInputStream,
									Type.WAVE, audioStream);

							fss.saveStream(keyname,
									"wav", //$NON-NLS-1$
									new ByteArrayInputStream(audioStream
											.toByteArray()));

						}

					}

				}
			}

			logger.debug("end of saving the pipestops samples"); //$NON-NLS-1$

			logger.debug("saving the percussion samples");

			{
				PercussionDef[] pdlist = scale.findUniquePercussionDefs();
				if (pdlist != null && pdlist.length > 0) {
					for (int i = 0; i < pdlist.length; i++) {
						PercussionDef percussionDef = pdlist[i];
						int pmidicode = percussionDef.getPercussion();

						SoundSample ss = model
								.getPercussionSoundSample(percussionDef);

						if (ss != null) {

							String keyname = pmidicode + "_" //$NON-NLS-1$
									+ StringTools.toHex(ss.getName()) + "_" //$NON-NLS-1$
									+ ss.getLoopStart() + "_" + ss.getLoopEnd(); //$NON-NLS-1$

							logger.debug("stream key name " + keyname); //$NON-NLS-1$

							ByteArrayOutputStream audioStream = new ByteArrayOutputStream();

							ManagedAudioInputStream managedAudioInputStream = ss
									.getManagedAudioInputStream();
							AudioSystem.write(managedAudioInputStream,
									Type.WAVE, audioStream);

							fss.saveStream(keyname,
									DRUMWAV, //$NON-NLS-1$
									new ByteArrayInputStream(audioStream
											.toByteArray()));
						} else {
							logger.debug("no sound sample associated to "
									+ percussionDef);
						}

					}

				} else {
					logger.debug("no percussion samples");
				}
			}

			if (model.getInstrumentPicture() != null
					&& model.getInstrumentPicture() instanceof BufferedImage) {

				logger.debug("saving image ... "); //$NON-NLS-1$
				BufferedImage bi = (BufferedImage) model.getInstrumentPicture();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageTools.saveJpeg(bi, baos); //$NON-NLS-1$

				fss.saveStream("image", "jpg", new ByteArrayInputStream(baos //$NON-NLS-1$ //$NON-NLS-2$
						.toByteArray()));

			}

			logger.debug("saving the instrument properties ..."); //$NON-NLS-1$

			Properties p = new Properties();

			p.setProperty("version", "1"); //$NON-NLS-1$ //$NON-NLS-2$

			if (model.getName() != null)
				p.setProperty("name", model.getName()); //$NON-NLS-1$

			if (model.getInstrumentDescription() != null)
				p.setProperty("description", model.getInstrumentDescription()); //$NON-NLS-1$

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			p.save(baos, "instrument properties"); //$NON-NLS-1$

			fss.saveStream("instrument", "properties", //$NON-NLS-1$ //$NON-NLS-2$
					new ByteArrayInputStream(baos.toByteArray()));

			logger.debug("saving the importer scripts ... ");

			InstrumentScript[] importerScripts = model.getScripts();
			for (int i = 0; i < importerScripts.length; i++) {
				InstrumentScript instrumentScript = importerScripts[i];

				logger.debug("saving importer script "
						+ instrumentScript.getName());

				if (instrumentScript.getScriptLanguage() != InstrumentScript.InstrumentScriptLanguage.GROOVY)
					throw new Exception("unsupported importer script format");

				String scriptstreamname = StringTools.toHex(instrumentScript
						.getName());

				ByteArrayOutputStream baosInstrumentImporterScript = new ByteArrayOutputStream();

				if (logger.isDebugEnabled())
					logger.debug("script content :"
							+ instrumentScript.getContent());

				StreamsTools.fullyWriteUTF8StringIntoStream(
						instrumentScript.getContent(),
						baosInstrumentImporterScript);

				String extension = null;

				if (instrumentScript.getType() == InstrumentScript.InstrumentScriptType.IMPORTER_SCRIPT) {
					extension = GROOVYIMPORTERSCRIPTSEXTENSION;
				} else if (instrumentScript.getType() == InstrumentScript.InstrumentScriptType.MIDI_OUTPUT_SCRIPT) {
					extension = GROOVYEXPORTERSCRIPTSEXTENSION;
				}

				if (extension == null)
					throw new Exception(
							"unknown instrument script type ... cannot save");

				fss.saveStream(
						scriptstreamname,
						extension,
						new ByteArrayInputStream(baosInstrumentImporterScript
								.toByteArray()));

				logger.debug("script " + instrumentScript + " written");
			}

			// supress all the sha1 elements ...
			fss.removeAllStreamDigestCaches();

			// Marshall the result ...

			ZipStreamMarshaller zsm = new ZipStreamMarshaller();
			zsm.pack(fss, os);

		} finally {
			StreamsTools.recurseDelete(createTempFile);
		}

	}

	/**
	 * Loading the instrument definition ...
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public IEditableInstrument load(InputStream is, String name)
			throws Exception {

		logger.debug("unpacking the instrument definition .... "); //$NON-NLS-1$

		File createTempFile = File.createTempFile("storagefoldertemp", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		createTempFile.delete();
		createTempFile.mkdirs();
		try {

			FolderStreamStorage fss = new FolderStreamStorage(createTempFile);

			ZipStreamMarshaller zsm = new ZipStreamMarshaller();
			zsm.unpack(is, fss);

			// reading the scale ...
			String[] scalesStreams = fss.listStreams("scale"); //$NON-NLS-1$
			if (scalesStreams.length <= 0)
				throw new Exception("no scale in stream ..."); //$NON-NLS-1$

			InputStream scaleStream = fss.openStream(scalesStreams[0]);
			Scale scale = ScaleIO.readGamme(scaleStream);

			EditableInstrument newModel = new EditableInstrument();
			newModel.setScale(scale);

			logger.debug("reading the notes sound samples");

			{
				// getting streams ....
				String[] soundsmapping = fss.listStreams("wav"); //$NON-NLS-1$
				for (int i = 0; i < soundsmapping.length; i++) {
					String currentName = soundsmapping[i];

					currentName = currentName.substring(0,
							currentName.lastIndexOf('.'));

					logger.debug("sound name :" + currentName); //$NON-NLS-1$

					String[] splittedName = currentName.split("_"); //$NON-NLS-1$
					String pipestopgroup = splittedName[0];
					logger.debug("pipeStopGroup " + pipestopgroup); //$NON-NLS-1$
					int firstMidiCode = Integer.parseInt(splittedName[1]);
					logger.debug("firstMidiCode :" + firstMidiCode); //$NON-NLS-1$
					int lastMidiCode = Integer.parseInt(splittedName[2]);
					logger.debug("lastMidiCode :" + lastMidiCode); //$NON-NLS-1$
					String soundSampleName = StringTools
							.fromHex(splittedName[3]);
					logger.debug("soundSampleName :" + soundSampleName); //$NON-NLS-1$
					long loopstart = Long.parseLong(splittedName[4]);
					logger.debug("loopStart :" + loopstart); //$NON-NLS-1$
					long loopEnd = Long.parseLong(splittedName[5]);
					logger.debug("loopEnd :" + loopEnd); //$NON-NLS-1$
					int midiRoot = Integer.parseInt(splittedName[6]);
					logger.debug("midiRoot :" + midiRoot); //$NON-NLS-1$

					InputStream wavStream = fss.openStream(soundsmapping[i]);

					ManagedAudioInputStream mais = new ManagedAudioInputStream(
							new ManagedAudioInputStream.NonManagedInputStream(
									AudioSystem.getAudioInputStream(wavStream)));

					SoundSample ss = new SoundSample(soundSampleName, midiRoot,
							mais);

					ss.setLoopStart(loopstart);
					ss.setLoopEnd(loopEnd);

					newModel.setSampleMapping(pipestopgroup, ss, firstMidiCode,
							lastMidiCode);
					newModel.addSoundSample(ss, pipestopgroup);

				}
			}
			{
				logger.debug("reading the percussion sound samples");
				// getting streams ....
				String[] percussionsmapping = fss.listStreams(DRUMWAV); //$NON-NLS-1$
				for (int i = 0; i < percussionsmapping.length; i++) {
					String currentName = percussionsmapping[i];

					currentName = currentName.substring(0,
							currentName.lastIndexOf('.'));

					logger.debug("sound name :" + currentName); //$NON-NLS-1$

					String[] splittedName = currentName.split("_"); //$NON-NLS-1$

					int percussionMidiCode = Integer.parseInt(splittedName[0]);
					logger.debug("percussion midi code :" + percussionMidiCode);
					String soundSampleName = StringTools
							.fromHex(splittedName[1]);
					logger.debug("soundSampleName :" + soundSampleName); //$NON-NLS-1$
					long loopstart = Long.parseLong(splittedName[2]);
					logger.debug("loopStart :" + loopstart); //$NON-NLS-1$
					long loopEnd = Long.parseLong(splittedName[3]);
					logger.debug("loopEnd :" + loopEnd); //$NON-NLS-1$

					InputStream wavStream = fss
							.openStream(percussionsmapping[i]);

					ManagedAudioInputStream mais = new ManagedAudioInputStream(
							new ManagedAudioInputStream.NonManagedInputStream(
									AudioSystem.getAudioInputStream(wavStream)));

					SoundSample ss = new SoundSample(soundSampleName,
							percussionMidiCode, mais);

					ss.setLoopStart(loopstart);
					ss.setLoopEnd(loopEnd);

					int p = scale.findPercussionDef(percussionMidiCode);
					if (p != -1) {
						PercussionDef pd = (PercussionDef) scale
								.getTracksDefinition()[p];

						newModel.setPercussionSoundSample(pd, ss);
						logger.debug("percussion sound sample defined");

					} else {
						logger.warn("percussion " + percussionMidiCode
								+ " not found in the scale");
					}
				}
			}

			logger.debug("reading instrument.properties");

			InputStream openStream = fss.openStream("instrument.properties"); //$NON-NLS-1$
			if (openStream != null) {
				Properties props = new Properties();
				props.load(openStream);
				String n = props.getProperty("name"); //$NON-NLS-1$
				if (n != null)
					newModel.setName(n);

				String description = props.getProperty("description"); //$NON-NLS-1$
				newModel.setInstrumentDescription(description);
			} else {
				newModel.setName(name);
			}

			logger.debug("reading the image");
			try {
				InputStream imageStream = fss.openStream("image.jpg"); //$NON-NLS-1$
				if (imageStream != null) {
					BufferedImage bi = ImageIO.read(imageStream);
					newModel.setInstrumentPicture(bi);
				}
			} catch (Exception ex) {
				logger.warn("cannot load image " + ex.getMessage(), ex); //$NON-NLS-1$
			}

			logger.debug("reading the importer scripts");

			readScript(fss, newModel, GROOVYIMPORTERSCRIPTSEXTENSION,
					InstrumentScript.InstrumentScriptType.IMPORTER_SCRIPT);

			readScript(fss, newModel, GROOVYEXPORTERSCRIPTSEXTENSION,
					InstrumentScript.InstrumentScriptType.MIDI_OUTPUT_SCRIPT);

			return newModel;

		} finally {
			StreamsTools.recurseDelete(createTempFile);
		}
	}

	private void readScript(FolderStreamStorage fss,
			EditableInstrument newModel, String extension,
			InstrumentScript.InstrumentScriptType type) {
		try {
			String[] groovyImporterScripts = fss.listStreams(extension); //$NON-NLS-1$
			for (int i = 0; i < groovyImporterScripts.length; i++) {
				String currentName = groovyImporterScripts[i];

				currentName = currentName.substring(0,
						currentName.lastIndexOf('.'));

				String scriptname = StringTools.fromHex(currentName);

				try {
					InputStream scriptStream = fss
							.openStream(groovyImporterScripts[i]);
					String scriptcontent = StreamsTools
							.fullyReadUTF8StringFromStream(scriptStream);

					InstrumentScript instrumentScript = new InstrumentScript(
							scriptname,
							InstrumentScript.InstrumentScriptLanguage.GROOVY,
							type, scriptcontent);

					newModel.addScript(instrumentScript);
				} catch (Throwable t) {
					logger.error("error while reading script :" + scriptname
							+ " , skipped , error :" + t.getMessage(), t);
				}

			}
		} catch (Throwable t) {
			logger.warn("error while reading the scripts of type " + type);
		}
	}

}
