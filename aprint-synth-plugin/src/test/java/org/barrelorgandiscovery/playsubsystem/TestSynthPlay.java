package org.barrelorgandiscovery.playsubsystem;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;

import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.editableinstrument.StreamStorageEditableInstrumentManager;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.repository.RepositoryException;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult;
import org.junit.Test;

import junit.framework.TestCase;
import net.frett27.synthetizer.Synthetizer;

public class TestSynthPlay extends TestCase {

	public static final String PERSONAL_EDITABLE_INSTRUMENTS = "Personal Editable Instruments :"; //$NON-NLS-1$

	private static EditableInstrumentManagerRepository2Adapter addFolderRepositoryToCollection(File theFolder)
			throws Exception, RepositoryException {

		FolderStreamStorage folderStreamStorage = new FolderStreamStorage(theFolder);

		StreamStorageEditableInstrumentManager eis = new StreamStorageEditableInstrumentManager(folderStreamStorage);

		File cacheFolder = new File(theFolder.getParentFile(), theFolder.getName() + ".cache"); //$NON-NLS-1$

		return new EditableInstrumentManagerRepository2Adapter(eis, PERSONAL_EDITABLE_INSTRUMENTS + theFolder.getName(),
				"folder :" + theFolder.getName(), //$NON-NLS-1$
				cacheFolder);
	}

	private EditableInstrumentManagerRepository2Adapter repository;

	@Override
	protected void setUp() throws Exception {

		File folder = new File("c:\\users\\use\\aprintstudio\\private");
		Properties repprop = new Properties();
		if (folder != null) {
			repprop.setProperty("folder", folder.getAbsolutePath()); //$NON-NLS-1$
		}

		repository = addFolderRepositoryToCollection(folder);

		System.out.println("Current instruments :" + Arrays.asList(repository.listInstruments()));

	}

	@Test
	public void testLoadAllSounds() throws Exception {

		assertTrue(repository instanceof EditableInstrumentManagerRepository);

		EditableInstrumentManagerRepository em = (EditableInstrumentManagerRepository) repository;

		String[] editableInstruments = em.getEditableInstrumentManager().listEditableInstruments();
		System.out.println(Arrays.asList(editableInstruments));
		IEditableInstrument einstrument = em.getEditableInstrumentManager()
				.loadEditableInstrument("24_thibouville_-244083455.instrumentbundle");
		assertNotNull(einstrument);

		String[] pipes = einstrument.getPipeStopGroupsAndRegisterName();
		System.out.println(Arrays.asList(pipes));

		List<SoundSample> l = einstrument.getSoundSampleList("DEFAULT");
		SoundSample s = l.get(1);

		float[] memorySound = SynthPlaySubSystem.loadSound(s);
		AudioFormat audioFormat = s.getFomat();

		Synthetizer synth = new Synthetizer();

		float noteFrequency = (float) MidiHelper.hertz(s.getMidiRootNote());
		long sid = synth.loadSample(memorySound, audioFormat.getSampleRate(), noteFrequency, false);
		synth.open();

		synth.getTime();

		Thread.sleep(3000);

		synth.play(sid, noteFrequency);
		Thread.sleep(500);
		synth.play(sid, noteFrequency * 2);

		Thread.sleep(4000);
	}

	@Test
	public void testPlayVirtualBookUsingSubSystem() throws Exception {
		assertTrue(repository instanceof EditableInstrumentManagerRepository);

		EditableInstrumentManagerRepository em = (EditableInstrumentManagerRepository) repository;

		VirtualBookResult r = VirtualBookXmlIO
				.read(new File("C:\\Users\\use\\Dropbox\\APrint\\Books\\Ariston\\le petit duc.book"));
		System.out.println(r.preferredInstrumentName);
		SynthPlaySubSystem pss = new SynthPlaySubSystem(em);
		Instrument ins = repository.getInstrument("Ariston 24 touches");
		assert ins != null;
		pss.setCurrentInstrument(ins);
		pss.play(null, r.virtualBook, new IPlaySubSystemFeedBack() {

			@Override
			public void playStopped() {
				// TODO Auto-generated method stub

			}

			@Override
			public void playStarted() {
				// TODO Auto-generated method stub

			}

			@Override
			public long informCurrentPlayPosition(long millis) {
				// TODO Auto-generated method stub
				return 0;
			}
		}, 0);
		Thread.sleep(1000);
		while (pss.isPlaying()) {
			Thread.sleep(1000);
			System.out.println("waiting ...");
		}
		System.out.println("End.");
		Thread.sleep(10_000);
	}
	
	@Test
	public void testMidiHertz() {
		System.out.println(MidiHelper.hertz(69));
	}

}
