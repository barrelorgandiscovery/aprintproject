package org.barrelorgandiscovery.editableinstrument;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript.InstrumentScriptType;
import org.barrelorgandiscovery.instrument.SampleMapping;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.PipeStop;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;

/**
 * Utility class for managing the structures associated to the Instrument Editor
 * ... this class can be passed to the several visual Components by reference
 * ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class EditableInstrument implements Disposable, IEditableInstrument {

	private static Logger logger = Logger.getLogger(EditableInstrument.class);

	public EditableInstrument() {

	}

	/**
	 * Scale associated to the instrument ...
	 */
	private Scale scale;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.editableinstrument.EditableInstrumentObject#setScale
	 * (fr.freydierepatrice.scale.Scale)
	 */
	public void setScale(Scale scale) {
		logger.debug("change scale ...");
		Scale oldScale = this.scale;
		this.scale = scale;

		for (Iterator iterator = scaleListeners.iterator(); iterator.hasNext();) {
			ScaleListener l = (ScaleListener) iterator.next();
			l.ScaleChanged(oldScale, scale);
		}

		toggleDirty();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.editableinstrument.EditableInstrumentObject#getScale
	 * ()
	 */
	public Scale getScale() {
		return scale;
	}

	/**
	 * Instrument Name
	 */
	private String name;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.editableinstrument.EditableInstrumentObject#setName
	 * (java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;

		for (Iterator iterator = descriptionListeners.iterator(); iterator
				.hasNext();) {
			InstrumentDescriptionListener l = (InstrumentDescriptionListener) iterator
					.next();
			if (l != null)
				l.instrumentNameChanged(name);
		}

		toggleDirty();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.editableinstrument.EditableInstrumentObject#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * getPipeStopGroups()
	 */
	public String[] getPipeStopGroupsAndRegisterName() {
		if (scale != null) {
			ArrayList<String> names = new ArrayList<String>();
			names.add(DEFAULT_PIPESTOPGROUPNAME); // convention toujours au debut
			PipeStopGroupList pipeStopGroupList = scale.getPipeStopGroupList();
			if (pipeStopGroupList != null) {
				for (int i = 0; i < pipeStopGroupList.size(); i++) {

					PipeStopGroup pipeStopGroup = pipeStopGroupList.get(i);

					// add a default pipe stop name in list ...
					names.add(pipeStopGroup.getName() + "-"
							+ DEFAULT_PIPESTOPGROUPNAME);

					PipeStop[] controlledPipeStops = pipeStopGroup
							.getRegisteredControlledPipeStops();
					for (int j = 0; j < controlledPipeStops.length; j++) {
						PipeStop pipeStop = controlledPipeStops[j];
						names.add(pipeStopGroup.getName() + "-"
								+ pipeStop.getName());
					}

				}
			}
			return names.toArray(new String[0]);
		}
		return new String[0];
	}

	/**
	 * Listeners for scale changes
	 */
	private Vector<ScaleListener> scaleListeners = new Vector<ScaleListener>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.editableinstrument.EditableInstrumentObject#addListener
	 * (fr.freydierepatrice.gui.ainstrument.ScaleListener)
	 */
	public void addListener(ScaleListener listener) {
		if (listener != null)
			scaleListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * removeListener(fr.freydierepatrice.gui.ainstrument.ScaleListener)
	 */
	public void removeListener(ScaleListener listener) {
		scaleListeners.remove(listener);
	}

	/**
	 * Reference of the sound samples , by pipestop group....
	 */
	private HashMap<String, List<SoundSample>> soundsample_by_pipestop = new HashMap<String, List<SoundSample>>();

	/**
	 * Listener to the sound sample list
	 */
	private Vector<SoundSampleListListener> soundSampleListListener = new Vector<SoundSampleListListener>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.editableinstrument.EditableInstrumentObject#addListener
	 * (fr.freydierepatrice.gui.ainstrument.SoundSampleListListener)
	 */
	public void addListener(SoundSampleListListener listener) {
		if (listener != null)
			soundSampleListListener.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * removeListener
	 * (fr.freydierepatrice.gui.ainstrument.SoundSampleListListener)
	 */
	public void removeListener(SoundSampleListListener listener) {
		soundSampleListListener.remove(listener);
	}

	private Vector<InstrumentDescriptionListener> descriptionListeners = new Vector<InstrumentDescriptionListener>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.editableinstrument.EditableInstrumentObject#addListener
	 * (fr.freydierepatrice.editableinstrument.InstrumentDescriptionListener)
	 */
	public void addListener(InstrumentDescriptionListener listener) {
		if (listener == null)
			return;
		descriptionListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * removeListener
	 * (fr.freydierepatrice.editableinstrument.InstrumentDescriptionListener)
	 */
	public void removeListener(InstrumentDescriptionListener listener) {
		if (listener == null)
			return;
		descriptionListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * getSoundSampleList(java.lang.String)
	 */
	public List<SoundSample> getSoundSampleList(String pipeStopGroup) {
		if (pipeStopGroup == null)
			pipeStopGroup = DEFAULT_PIPESTOPGROUPNAME;
		List<SoundSample> list = soundsample_by_pipestop.get(pipeStopGroup);
		if (list == null) {
			// return empty
			list = new ArrayList<SoundSample>();
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * addSoundSample(fr.freydierepatrice.instrument.sample.SoundSample,
	 * java.lang.String)
	 */
	public void addSoundSample(SoundSample ss, String pipeStopGroup) {

		if (pipeStopGroup == null)
			pipeStopGroup = IEditableInstrument.DEFAULT_PIPESTOPGROUPNAME;

		if (ss != null) {
			List<SoundSample> list = soundsample_by_pipestop.get(pipeStopGroup);
			if (list == null)
				list = new ArrayList<SoundSample>();

			list.add(ss);

			soundsample_by_pipestop.put(pipeStopGroup, list);
		}

		for (Iterator iterator = soundSampleListListener.iterator(); iterator
				.hasNext();) {
			SoundSampleListListener l = (SoundSampleListListener) iterator
					.next();
			l.soundSampleAdded(ss, pipeStopGroup);
		}

		toggleDirty();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * removeSoundSample(fr.freydierepatrice.instrument.sample.SoundSample,
	 * java.lang.String)
	 */
	public void removeSoundSample(SoundSample ss, String pipeStopGroup) {

		if (pipeStopGroup == null)
			pipeStopGroup = DEFAULT_PIPESTOPGROUPNAME;

		if (ss != null) {
			List<SoundSample> list = soundsample_by_pipestop.get(pipeStopGroup);
			if (list != null)
				list.remove(ss);

			soundsample_by_pipestop.put(pipeStopGroup, list);
		}
		for (Iterator iterator = soundSampleListListener.iterator(); iterator
				.hasNext();) {
			SoundSampleListListener l = (SoundSampleListListener) iterator
					.next();
			l.soundSampleRemoved(ss, pipeStopGroup);
		}

		toggleDirty();
	}

	/**
	 * Mémorisation des mappings
	 */
	private HashMap<String, HashMap<SoundSample, SampleMapping>> mappings = new HashMap<String, HashMap<SoundSample, SampleMapping>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * getSampleMapping(java.lang.String,
	 * fr.freydierepatrice.instrument.sample.SoundSample)
	 */
	public SampleMapping getSampleMapping(String pipeStopGroupAndRegister,
			SoundSample sample) {

		HashMap<SoundSample, SampleMapping> samplemappings = mappings
				.get(pipeStopGroupAndRegister);
		if (samplemappings == null)
			return null;

		return samplemappings.get(sample);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * setSampleMapping(java.lang.String,
	 * fr.freydierepatrice.instrument.sample.SoundSample, int, int)
	 */
	public SampleMapping setSampleMapping(String pipeStopGroup,
			SoundSample sample, int first, int end) {

		HashMap<SoundSample, SampleMapping> hashMap = mappings
				.get(pipeStopGroup);
		if (hashMap == null) {
			hashMap = new HashMap<SoundSample, SampleMapping>();
			mappings.put(pipeStopGroup, hashMap);
		}

		SampleMapping smapping = new SampleMapping(sample, first, end);
		hashMap.put(sample, smapping);

		toggleDirty();

		return smapping;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * removeSampleMapping(java.lang.String,
	 * fr.freydierepatrice.instrument.sample.SoundSample)
	 */
	public void removeSampleMapping(String pipeStopGroup, SoundSample sample) {
		HashMap<SoundSample, SampleMapping> hashMap = mappings
				.get(pipeStopGroup);
		if (hashMap == null) {
			return;
		}

		hashMap.remove(sample);

		toggleDirty();
	}

	private String instrumentDescription = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * getInstrumentDescription()
	 */
	public String getInstrumentDescription() {
		return instrumentDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * setInstrumentDescription(java.lang.String)
	 */
	public void setInstrumentDescription(String instrumentDescription) {
		this.instrumentDescription = instrumentDescription;

		for (Iterator iterator = descriptionListeners.iterator(); iterator
				.hasNext();) {
			InstrumentDescriptionListener l = (InstrumentDescriptionListener) iterator
					.next();
			if (l != null)
				l.instrumentDescriptionChanged(name);
		}

		toggleDirty();

	}

	private Image instrumentPicture = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * setInstrumentPicture(java.awt.Image)
	 */
	public void setInstrumentPicture(Image instrumentPicture) {
		this.instrumentPicture = instrumentPicture;

		for (Iterator iterator = descriptionListeners.iterator(); iterator
				.hasNext();) {
			InstrumentDescriptionListener l = (InstrumentDescriptionListener) iterator
					.next();
			if (l != null)
				l.instrumentImageChanged(instrumentPicture);
		}

		toggleDirty();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.editableinstrument.EditableInstrumentObject#
	 * getInstrumentPicture()
	 */
	public Image getInstrumentPicture() {
		return instrumentPicture;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.editableinstrument.EditableInstrumentObject#dispose()
	 */
	public void dispose() {
		logger.debug("dispose ...");
		for (Iterator iterator = soundsample_by_pipestop.values().iterator(); iterator
				.hasNext();) {
			List<SoundSample> lss = (List<SoundSample>) iterator.next();
			if (lss != null) {

				for (Iterator iterator2 = lss.iterator(); iterator2.hasNext();) {
					SoundSample soundSample = (SoundSample) iterator2.next();
					soundSample.dispose();

				}
			}
		}

		for (Iterator<SoundSample> iterator = percussionSamples.values()
				.iterator(); iterator.hasNext();) {
			SoundSample ss = (SoundSample) iterator.next();
			if (ss != null)
				ss.dispose();
		}

	}

	/**
	 * managing the dirty flag
	 */
	private boolean dirty = false;

	/**
	 * is the object has been modified since last clear ?
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * clear instrument modified status
	 */
	public void clearDirty() {
		this.dirty = false;
	}

	/**
	 * signal instrument is dirty
	 */
	protected void toggleDirty() {
		dirty = true;
	}

	/**
	 * Importer scripts associated to the instruments
	 */
	private ArrayList<InstrumentScript> midiAssociatedScripts = new ArrayList<InstrumentScript>();

	/**
	 * Add a script into the instrument
	 * 
	 * @param script
	 */
	public void addScript(InstrumentScript script) {

		assert script != null;
		assert findScript(script.getName()) == null;

		midiAssociatedScripts.add(script);
		toggleDirty();

	}

	/**
	 * Remove a script from the instrument
	 * 
	 * @param name
	 */
	public void removeScript(String name) {

		if (name == null)
			return;

		InstrumentScript found = null;

		for (Iterator iterator = midiAssociatedScripts.iterator(); iterator
				.hasNext();) {
			InstrumentScript script = (InstrumentScript) iterator.next();
			if (name.equals(script.getName())) {
				found = script;
			}
		}

		if (found != null) {
			midiAssociatedScripts.remove(found);
			toggleDirty();
		}
	}

	/**
	 * Find a script associated to the instrument
	 * 
	 * @param name
	 * @return
	 */
	public InstrumentScript findScript(String name) {
		if (name == null)
			return null;

		for (Iterator iterator = midiAssociatedScripts.iterator(); iterator
				.hasNext();) {
			InstrumentScript script = (InstrumentScript) iterator.next();
			if (name.equals(script.getName()))
				return script;
		}

		return null;
	}

	public InstrumentScript[] findScriptsByType(InstrumentScriptType type) {

		ArrayList<InstrumentScript> scripts = new ArrayList<InstrumentScript>();

		for (Iterator iterator = midiAssociatedScripts.iterator(); iterator
				.hasNext();) {
			InstrumentScript script = (InstrumentScript) iterator.next();

			if (logger.isDebugEnabled())
				logger.debug("evaluating script :" + script);

			if (type == script.getType()) {
				logger.debug("adding script to result");
				scripts.add(script);
			}
		}

		return scripts.toArray(new InstrumentScript[0]);

	}

	/**
	 * get all the importer scripts asssociated to the instrument
	 * 
	 * @return
	 */
	public InstrumentScript[] getScripts() {
		return midiAssociatedScripts.toArray(new InstrumentScript[0]);
	}

	// ////////////////////////////////////////////////////////////////
	// Percussion handling

	private HashMap<Integer, SoundSample> percussionSamples = new HashMap<Integer, SoundSample>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.editableinstrument.IEditableInstrument#
	 * setPercussionSoundSample(org.barrelorgandiscovery.scale.PercussionDef,
	 * org.barrelorgandiscovery.instrument.sample.SoundSample)
	 */
	public void setPercussionSoundSample(PercussionDef pd, SoundSample ss) {

		if (pd == null)
			throw new IllegalArgumentException("nul percussion def passed");

		SoundSample ssold = percussionSamples.get(pd.getPercussion());
		if (ssold != null)
			ssold.dispose(); // release the old percussion

		logger.debug("setting the percussion sample " + ss + " on "
				+ pd.getPercussion());

		if (ss == null) {
			percussionSamples.remove(pd.getPercussion());
		} else {
			percussionSamples.put(pd.getPercussion(), ss);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.editableinstrument.IEditableInstrument#
	 * getPercussionSoundSample(org.barrelorgandiscovery.scale.PercussionDef)
	 */
	public SoundSample getPercussionSoundSample(PercussionDef pd) {

		if (pd == null)
			throw new IllegalArgumentException("nul percussion def passed");

		return percussionSamples.get(pd.getPercussion());
	}

}
