package org.barrelorgandiscovery.editableinstrument;

import java.awt.Image;
import java.util.List;

import org.barrelorgandiscovery.editableinstrument.InstrumentScript.InstrumentScriptType;
import org.barrelorgandiscovery.instrument.SampleMapping;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.Scale;

public interface IEditableInstrument {

	/**
	 * Default pipestopgroupname
	 */
	public static String DEFAULT_PIPESTOPGROUPNAME = "DEFAULT"; //$NON-NLS-1$

	/**
	 * Define a new Scale associated to the instrument
	 */
	public abstract void setScale(Scale scale);

	/**
	 * Get the scale of the instrument
	 * @return
	 */
	public abstract Scale getScale();

	/**
	 * Define the name of the instrument
	 */
	public abstract void setName(String name);

	/**
	 * Get the instrument name
	 * @return
	 */
	public abstract String getName();

	/**
	 * Get the list of pipestop groups in the instrument
	 * @return
	 */
	public abstract String[] getPipeStopGroupsAndRegisterName();

	/**
	 * Add a scale listener ...
	 * 
	 * @param listener
	 */
	public abstract void addListener(ScaleListener listener);

	/**
	 * Remove a scale listener
	 * 
	 * @param listener
	 */
	public abstract void removeListener(ScaleListener listener);

	/**
	 * Add sound sample list listener
	 * @param listener
	 */
	public abstract void addListener(SoundSampleListListener listener);

	/**
	 * Remove sound sample list listener
	 */
	public abstract void removeListener(SoundSampleListListener listener);

	public abstract void addListener(InstrumentDescriptionListener listener);

	public abstract void removeListener(InstrumentDescriptionListener listener);

	/**
	 * Get the sound sample List ...
	 * 
	 * @return
	 */
	public abstract List<SoundSample> getSoundSampleList(String pipeStopGroup);

	public abstract void addSoundSample(SoundSample ss, String pipeStopGroup);

	
	public abstract void removeSoundSample(SoundSample ss, String pipeStopGroup);

	/**
	 * Get the mapping associated with a pipestopgroup
	 * 
	 * @param pipeStopGroup
	 * @param sample
	 * @return
	 */
	public abstract SampleMapping getSampleMapping(String pipeStopGroup,
			SoundSample sample);

	/**
	 * Define a sound sample for a pipestopgroup
	 * @param pipeStopGroup the pipestopgroup
	 * @param sample the sample
	 * @param first the first note of the sample association
	 * @param end the last note of the sample association
	 * @return
	 */
	public abstract SampleMapping setSampleMapping(String pipeStopGroup,
			SoundSample sample, int first, int end);

	/**
	 * Remove a sound sample associated to a pipestopgroup
	 * @param pipeStopGroup
	 * @param sample
	 */
	public abstract void removeSampleMapping(String pipeStopGroup,
			SoundSample sample);

	/**
	 * Get the instrument description
	 * @return
	 */
	public abstract String getInstrumentDescription();

	/**
	 * Define the instrument description
	 * @param instrumentDescription
	 */
	public abstract void setInstrumentDescription(String instrumentDescription);

	/**
	 * Define the instrument picture
	 * @param instrumentPicture
	 */
	public abstract void setInstrumentPicture(Image instrumentPicture);

	/**
	 * Get the instrument picture
	 * @return
	 */
	public abstract Image getInstrumentPicture();

	/**
	 * Release all the instrument resources used
	 */
	public abstract void dispose();

	/**
	 * Ask if the instrument is dirty
	 * @return
	 */
	public abstract boolean isDirty();

	/**
	 * Reset the dirty flag
	 */
	public abstract void clearDirty();

	
	// Scripts handling
	
	/**
	 * Get all the scripts associated to an instrument
	 */
	public InstrumentScript[] getScripts();

	/**
	 * Find a script by its name
	 * @param name
	 * @return
	 */
	public InstrumentScript findScript(String name);
	
	/**
	 * Find all the script of a specified type
	 * @param type
	 * @return
	 */
	public InstrumentScript[] findScriptsByType(InstrumentScriptType type);

	/**
	 * Remove a script by its name
	 * @param name
	 */
	public void removeScript(String name);

	/**
	 * Add an instrument script
	 * @param script
	 */
	public void addScript(InstrumentScript script);
	
	// Percussion Samples Handling
	
	/**
	 * Get the sample associated to a percussion sample
	 */
	public SoundSample getPercussionSoundSample(PercussionDef pd);
	
	/**
	 * define a sample associated to a percussion
	 * @param pd
	 * @param ss
	 */
	public void setPercussionSoundSample(PercussionDef pd, SoundSample ss);
	
	

}