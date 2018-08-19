package org.barrelorgandiscovery.gaerepositoryclient;

import java.awt.Image;
import java.util.List;

import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.editableinstrument.InstrumentDescriptionListener;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript;
import org.barrelorgandiscovery.editableinstrument.ScaleListener;
import org.barrelorgandiscovery.editableinstrument.SoundSampleListListener;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript.InstrumentScriptType;
import org.barrelorgandiscovery.instrument.SampleMapping;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.Scale;

/**
 * Editable Instrument that support ID / Version for the repository link ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class SynchronizedEditableInstrument implements IEditableInstrument {

	private IEditableInstrument wrappedEditableInstrument;

	private long id = -1;

	private long version = -1;

	private String status = null;

	private String referenceName;

	public SynchronizedEditableInstrument(
			IEditableInstrument wrappedEditableInstrument, long id,
			long version, String status, String referenceName) {
		super();

		this.wrappedEditableInstrument = wrappedEditableInstrument;
		this.id = id;
		this.version = version;
		this.status = status;
		this.referenceName = referenceName;
	}

	public void addListener(ScaleListener listener) {
		wrappedEditableInstrument.addListener(listener);
	}

	public void addListener(SoundSampleListListener listener) {
		wrappedEditableInstrument.addListener(listener);

	}

	public void addListener(InstrumentDescriptionListener listener) {
		wrappedEditableInstrument.addListener(listener);

	}

	public void addSoundSample(SoundSample ss, String pipeStopGroup) {
		wrappedEditableInstrument.addSoundSample(ss, pipeStopGroup);

	}

	public void dispose() {
		wrappedEditableInstrument.dispose();

	}

	public String getInstrumentDescription() {
		return wrappedEditableInstrument.getInstrumentDescription();
	}

	public Image getInstrumentPicture() {
		return wrappedEditableInstrument.getInstrumentPicture();
	}

	public String getName() {
		return wrappedEditableInstrument.getName();
	}

	public String[] getPipeStopGroupsAndRegisterName() {
		return wrappedEditableInstrument.getPipeStopGroupsAndRegisterName();
	}

	public SampleMapping getSampleMapping(String pipeStopGroup,
			SoundSample sample) {
		return wrappedEditableInstrument
				.getSampleMapping(pipeStopGroup, sample);
	}

	public Scale getScale() {
		return wrappedEditableInstrument.getScale();
	}

	public List<SoundSample> getSoundSampleList(String pipeStopGroup) {
		return wrappedEditableInstrument.getSoundSampleList(pipeStopGroup);
	}

	public void removeListener(ScaleListener listener) {
		wrappedEditableInstrument.removeListener(listener);
	}

	public void removeListener(SoundSampleListListener listener) {
		wrappedEditableInstrument.removeListener(listener);

	}

	public void removeListener(InstrumentDescriptionListener listener) {
		wrappedEditableInstrument.removeListener(listener);

	}

	public void removeSampleMapping(String pipeStopGroup, SoundSample sample) {
		wrappedEditableInstrument.removeSampleMapping(pipeStopGroup, sample);

	}

	public void removeSoundSample(SoundSample ss, String pipeStopGroup) {
		wrappedEditableInstrument.removeSoundSample(ss, pipeStopGroup);

	}

	public void setInstrumentDescription(String instrumentDescription) {
		wrappedEditableInstrument
				.setInstrumentDescription(instrumentDescription);

	}

	public void setInstrumentPicture(Image instrumentPicture) {
		wrappedEditableInstrument.setInstrumentPicture(instrumentPicture);

	}

	public void setName(String name) {
		wrappedEditableInstrument.setName(name);

	}

	public SampleMapping setSampleMapping(String pipeStopGroup,
			SoundSample sample, int first, int end) {
		return wrappedEditableInstrument.setSampleMapping(pipeStopGroup,
				sample, first, end);
	}

	public void setScale(Scale scale) {
		wrappedEditableInstrument.setScale(scale);

	}

	public Long getRepositoryInstrumentID() {
		return this.id;
	}

	public Long getVersion() {
		return version;
	}

	public String getStatus() {
		return status;
	}

	public void changeStatus(String newStatus) {
		this.status = newStatus;
	}

	public String getReferenceName() {
		return referenceName;
	}

	public boolean isDirty() {
		return wrappedEditableInstrument.isDirty();
	}

	public void clearDirty() {
		wrappedEditableInstrument.clearDirty();
	}

	public void addScript(InstrumentScript script) {
		wrappedEditableInstrument.addScript(script);

	}

	public InstrumentScript findScript(String name) {
		return wrappedEditableInstrument.findScript(name);
	}

	public InstrumentScript[] getScripts() {
		return wrappedEditableInstrument.getScripts();
	}

	public void removeScript(String name) {
		wrappedEditableInstrument.removeScript(name);
	}

	public InstrumentScript[] findScriptsByType(InstrumentScriptType type) {
		return wrappedEditableInstrument.findScriptsByType(type);
	}

	public SoundSample getPercussionSoundSample(PercussionDef pd) {
		return wrappedEditableInstrument.getPercussionSoundSample(pd);
	}

	public void setPercussionSoundSample(PercussionDef pd, SoundSample ss) {
		wrappedEditableInstrument.setPercussionSoundSample(pd, ss);
	}

}
