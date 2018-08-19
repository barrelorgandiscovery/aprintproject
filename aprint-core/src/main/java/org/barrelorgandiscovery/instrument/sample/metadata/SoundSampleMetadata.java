package org.barrelorgandiscovery.instrument.sample.metadata;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

/**
 * Informations about a sample
 * 
 * @author use
 * 
 */
public class SoundSampleMetadata implements Externalizable {

	private static int version = 1;

	/**
	 * Name of the sound
	 */
	private String name;

	/**
	 * Key of the recording, out of scope for the drums
	 */
	private int rootKey;

	/**
	 * Loop definition start
	 */
	private long loopStart;

	/**
	 * Loop end
	 */
	private long loopEnd;

	/**
	 * Additional informations about the sample
	 */
	private String description;

	/**
	 * Date of the recording
	 */
	private Date recordedDate;

	/**
	 * Information about the sound recording
	 */
	private String recordingInformation;

	/**
	 * Name of the manufacturer of the pipe / drum
	 */
	private String recordedInstrument;

	/**
	 * Author of the recording
	 */
	private String author;

	/**
	 * String defining the pipe stop
	 */
	private String pipeStopCode;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRootKey() {
		return rootKey;
	}

	public void setRootKey(int rootKey) {
		this.rootKey = rootKey;
	}

	public long getLoopStart() {
		return loopStart;
	}

	public void setLoopStart(long loopStart) {
		this.loopStart = loopStart;
	}

	public long getLoopEnd() {
		return loopEnd;
	}

	public void setLoopEnd(long loopEnd) {
		this.loopEnd = loopEnd;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getRecordedDate() {
		return recordedDate;
	}

	public void setRecordedDate(Date recordedDate) {
		this.recordedDate = recordedDate;
	}

	public String getRecordedInstrument() {
		return recordedInstrument;
	}

	public void setRecordedInstrument(String recordedInstrument) {
		this.recordedInstrument = recordedInstrument;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {

		in.readInt(); // version

		this.name = (String) in.readObject();
		this.rootKey = in.readInt();
		this.loopStart = in.readLong();
		this.loopEnd = in.readLong();
		this.description = (String) in.readObject();
		this.recordedDate = (Date) in.readObject();
		this.recordedInstrument = (String) in.readObject();
		this.author = (String) in.readObject();
		this.recordingInformation = (String) in.readObject();
		this.pipeStopCode = (String) in.readObject();

	}

	public void writeExternal(ObjectOutput out) throws IOException {
		
		out.writeInt(version);
		
		out.writeObject(name);
		out.writeInt(rootKey);
		out.writeLong(loopStart);
		out.writeLong(loopEnd);
		out.writeObject(description);
		out.writeObject(recordedDate);
		out.writeObject(recordedInstrument);
		out.writeObject(author);
		out.writeObject(recordingInformation);
		out.writeObject(pipeStopCode);

	}

}
